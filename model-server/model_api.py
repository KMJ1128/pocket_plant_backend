"""Authenticated PC inference endpoint for the Pocket Plant AWS proxy."""
import gc
import hmac
import io
import logging
import os
import sys
from pathlib import Path
import tempfile
import threading

from fastapi import Depends, FastAPI, File, Form, Header, HTTPException, UploadFile
from PIL import Image, UnidentifiedImageError
from starlette.concurrency import run_in_threadpool

ROOT = Path(os.environ.get('PLANT_MODEL_ROOT', str(Path(__file__).resolve().parent))).resolve()
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))
TOKEN = os.environ.get('PLANT_MODEL_API_TOKEN', '')
if len(TOKEN) < 32:
    raise RuntimeError('Set PLANT_MODEL_API_TOKEN to a secret of at least 32 characters.')
MAX_BYTES = 10 * 1024 * 1024
Image.MAX_IMAGE_PIXELS = 20_000_000
SPECIES = {
    'Pothos': ('스킨답서스', 'Epipremnum aureum'),
    'Monstera': ('몬스테라', 'Monstera deliciosa'),
    'SnakePlant': ('산세베리아', 'Dracaena trifasciata', 'Sansevieria trifasciata'),
    'SpiderPlant': ('접란', 'Chlorophytum comosum'),
    'AloeVera': ('알로에', '알로에 베라', 'Aloe vera'),
    'Opuntia': ('백년초', 'Opuntia'),
    'Tomato': ('토마토', 'Solanum lycopersicum'),
    'Pepper,_bell': ('피망', '파프리카'),
    'Strawberry': ('딸기', 'Fragaria'),
}
ALIASES = {alias.casefold().strip(): key for key, aliases in SPECIES.items()
           for alias in (key, *aliases)}


def canonical_species(value):
    # Explicit aliases only; do not silently map stuki or other species to SnakePlant.
    return ALIASES.get(value.strip().casefold())


def authenticate(x_model_token: str = Header(default='')):
    if not hmac.compare_digest(x_model_token.encode(), TOKEN.encode()):
        raise HTTPException(401, '모델 서버 인증에 실패했습니다.')


class InferenceEngine:
    """Same observation + candidate reconciliation used by the desktop app."""
    def __init__(self):
        self.visual = self.monstera = self.classifier = None
        self.lock = threading.Lock()

    def release_visual(self):
        self.visual = self.monstera = None
        gc.collect()
        import torch
        torch.cuda.empty_cache()

    def predict(self, path, species):
        if not self.lock.acquire(blocking=False):
            raise HTTPException(429, '다른 사진을 분석 중입니다. 잠시 후 다시 시도하세요.')
        try:
            if species == 'Monstera':
                if self.visual is not None:
                    self.release_visual()
                if self.monstera is None:
                    from monstera_differential import MonsteraDifferential
                    self.monstera = MonsteraDifferential(predictive=True)
                return {'monstera_differential': self.monstera.predict(path, species)}
            if self.monstera is not None:
                self.release_visual()
            if self.visual is None:
                from visual_analyzer import PlantAnalyzer
                self.visual = PlantAnalyzer(str(ROOT / 'models/vision_v2'))
            result = self.visual.predict(path, species)
            if result.get('status') == 'observation':
                if self.classifier is None:
                    from disease_candidates import DiseaseCandidatePredictor
                    self.classifier = DiseaseCandidatePredictor()
                candidates = self.classifier.predict(path, species)
                from candidate_explanations import reconcile_observations
                result['disease_candidates'] = reconcile_observations(candidates, result.get('observations', []))
            return result
        finally:
            self.lock.release()


def public_result(result, species):
    """Allowlist fields; never return raw prompts, file paths or invented probability."""
    from candidate_explanations import guide_for, render_explanation
    output = {'species': species, 'confidence': None, 'candidates': [],
              'model_version': 'ornamental-v17-api2', 'score_kind': 'unavailable',
              'status': result.get('status', 'uncertain'),
              'disease_symptom': result.get('message', '사진의 원인을 구분하지 못했습니다.'),
              'observation': result.get('description', ''), 'features': '', 'actions': ''}
    differential = result.get('monstera_differential')
    if differential:
        from monstera_differential import render_differential
        from plant_guidance import MONSTERA_GUIDANCE, with_cause
        rows = differential.get('candidates', [])
        output.update(status='experimental', analysis_text=render_differential(differential))
        for row in rows[:3]:
            guide = MONSTERA_GUIDANCE.get(row.get('code'))
            if guide:
                candidate = with_cause(guide)
                candidate['features'] = '사진에서 본 모습: ' + row.get('visible', '') + '\n' + candidate['features']
                candidate['actions'] += '\n추가 확인: ' + row.get('missing', '')
            else:
                candidate = {'name': row['name'], 'features': row.get('visible', ''), 'actions': row.get('missing', '')}
            output['candidates'].append({key: candidate[key] for key in ('name', 'features', 'actions')} | {'comparison_score': None})
        if output['candidates']:
            top = output['candidates'][0]
            output.update(disease_symptom=top['name'], features=top['features'], actions=top['actions'])
        return output
    data = result.get('disease_candidates')
    if not data:
        return output
    output['analysis_text'] = render_explanation(data)
    blocked = bool(data.get('normal_result_blocked'))
    output['normal_result_blocked'] = blocked
    if blocked:
        from plant_guidance import blocked_guidance, with_cause
        guide = with_cause(blocked_guidance(species))
        output.update(status='uncertain', disease_symptom=guide['name'],
                      features=guide['features'], actions=guide['actions'])
        return output
    rows = data.get('candidates', [])
    if data.get('status') != 'experimental_candidates' or not rows:
        output.update(status='uncertain', disease_symptom=data.get('message', output['disease_symptom']))
        return output
    output.update(status='experimental', score_kind='relative_model_score')
    output['candidates'] = [dict(guide_for(row), comparison_score=row['comparison_score'],
                                 source_label=row['label']) for row in rows[:3]]
    top = output['candidates'][0]
    output.update(disease_symptom=top['name'], source_label=top['source_label'],
                  features=top['features'], actions=top['actions'])
    return output


app = FastAPI(docs_url=None, redoc_url=None, openapi_url=None,
              dependencies=[Depends(authenticate)])
engine = InferenceEngine()


@app.middleware('http')
async def guard_request(request, call_next):
    from starlette.responses import JSONResponse
    # Reject unauthenticated uploads before multipart parsing or disk spooling.
    supplied = request.headers.get('x-model-token', '')
    if not hmac.compare_digest(supplied.encode(), TOKEN.encode()):
        return JSONResponse({'detail': '모델 서버 인증에 실패했습니다.'}, status_code=401)
    length = request.headers.get('content-length')
    try:
        if length is not None and int(length) > MAX_BYTES + 64 * 1024:
            return JSONResponse({'detail': '사진은 10MB 이하로 올려 주세요.'}, status_code=413)
    except ValueError:
        return JSONResponse({'detail': '잘못된 요청 크기입니다.'}, status_code=400)
    # Bound even chunked bodies before FastAPI's multipart parser consumes them.
    if request.method == 'POST':
        chunks, total = [], 0
        async for chunk in request.stream():
            total += len(chunk)
            if total > MAX_BYTES + 64 * 1024:
                return JSONResponse({'detail': '사진은 10MB 이하로 올려 주세요.'}, status_code=413)
            chunks.append(chunk)
        request._body = b''.join(chunks)
    return await call_next(request)


@app.get('/health')
def health():
    return {'status': 'ok', 'busy': engine.lock.locked(), 'model_version': 'ornamental-v17-api2'}


@app.get('/supported-species')
def supported_species():
    return {'species': [{'id': key, 'name': aliases[0]} for key, aliases in SPECIES.items()]}


@app.post('/predict')
async def predict(image: UploadFile = File(...), species: str = Form('')):
    selected = canonical_species(species)
    if not selected:
        raise HTTPException(422, '지원하는 식물 종류를 선택하세요. 스투키 등 다른 종은 지원하지 않습니다.')
    content = await image.read(MAX_BYTES + 1)
    if len(content) > MAX_BYTES:
        raise HTTPException(413, '사진은 10MB 이하로 올려 주세요.')
    try:
        with Image.open(io.BytesIO(content)) as photo:
            if photo.format not in {'JPEG', 'PNG', 'WEBP'}:
                raise HTTPException(415, 'JPEG, PNG, WebP 사진을 올려 주세요.')
            if photo.width * photo.height > Image.MAX_IMAGE_PIXELS:
                raise HTTPException(413, '사진은 2천만 화소 이하로 올려 주세요.')
            photo.verify()
    except (UnidentifiedImageError, OSError, SyntaxError, Image.DecompressionBombError):
        raise HTTPException(415, '읽을 수 없는 사진입니다.')
    uploads = ROOT / '.runtime/uploads'
    uploads.mkdir(parents=True, exist_ok=True)
    path = None
    try:
        with tempfile.NamedTemporaryFile(dir=uploads, suffix='.img', delete=False) as file:
            path = Path(file.name)
            file.write(content)
        result = await run_in_threadpool(engine.predict, str(path), selected)
        return public_result(result, selected)
    except HTTPException:
        raise
    except Exception:
        logging.exception('Model inference failed')
        raise HTTPException(503, '모델 분석에 실패했습니다. 잠시 후 다시 시도하세요.')
    finally:
        if path:
            path.unlink(missing_ok=True)
