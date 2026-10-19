# Local model API

This directory contains the authenticated HTTP adapter for the existing PC plant model project. Model weights, expert reference assets, training data and private deployment configuration are not included in this repository.

Set `PLANT_MODEL_ROOT` to the existing model project directory. It must contain `visual_analyzer.py`, `disease_candidates.py`, `candidate_explanations.py`, `monstera_differential.py`, their supporting modules, and the validated `models/` and reference data used by those modules. Install `requirements.txt` **in that project's existing Python environment**; do not replace its PyTorch installation.

The adapter now includes reviewed Korean guidance for the five learned ornamental species and Monstera. Copy `candidate_explanations.py`, `plant_guidance.py`, and `monstera_differential.py` from this directory into `PLANT_MODEL_ROOT` when updating an existing model installation. The external model root is first on the module search path; leaving old support modules there would leave old guidance active. Back up local versions before updating. No new model weights are included or silently selected.

Set `PLANT_MODEL_API_TOKEN` to a private random secret of at least 32 characters, then run this adapter from this directory:

```powershell
$env:PLANT_MODEL_ROOT = 'D:\path\to\plant-model'
# Load PLANT_MODEL_API_TOKEN from a local private file or secret manager.
& "$env:PLANT_MODEL_ROOT\.venv\Scripts\python.exe" -m uvicorn model_api:app --host 127.0.0.1 --port 8765 --workers 1
```

`POST /predict` accepts multipart `image` and `species`. `GET /health` and `GET /supported-species` are also available. Every request requires `X-Model-Token`. Keep one worker to serialize GPU inference. The API accepts JPEG/PNG/WebP photos up to 10 MiB and 20 million pixels and removes temporary uploads.

The response contains `disease_symptom`, `candidates`, `features`, `actions`, `observation`, `score_kind`, `normal_result_blocked`, and `model_version`. `confidence` remains null until disease probabilities are calibrated. Candidate comparison scores are relative preferences within the selected species' learned classes. Monstera has a visual hypothesis method without numerical probability. Damage observations cancel a contradictory healthy verdict.

`features` separates image observations from the candidate's usual signs and possible causes. `actions` contains simple ordered next steps. This keeps compatibility with the existing Spring DTO. When visible damage contradicts the healthy classifier, the response keeps an empty candidate ranking and returns practical inspection steps; it does not invent a rot diagnosis or probability. API revision `ornamental-v17-api2` updates guidance while retaining the accepted v17 disease weights.

## AWS connection

The Spring endpoint remains `/api/disease/predict` and retains user JWT authentication. Configure `FAST_API_URL` to the HTTPS gateway's `/predict` address and `FAST_API_TOKEN` to the matching model secret. Do not include the model token in mobile code or commit it.

The current PC connection uses Cloudflare Quick Tunnel behind a fixed Workers gateway. Quick Tunnel is for testing and depends on the PC and tunnel process staying online; it is not a production availability guarantee. Gateway configuration and signing keys are maintained separately from the application repositories. The application's existing OpenAI chat integration is separate from this model API.

Frontend local web execution can exercise the same disease screens through its loopback model proxy. That preview does not bypass the AWS login flow: a signed-in mobile session is still required to test the complete mobile → AWS endpoint path.
