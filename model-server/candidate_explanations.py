"""Reviewed guidance, separate from signs actually detected in the image."""
TOMATO_SOURCE='https://extension.umd.edu/resource/key-common-problems-tomatoes'
SNAKE_SOURCE='https://ask.extension.org/kb/faq.php?id=861775'
GUIDES={
 'Septoria_leaf_spot':('셉토리아 잎반점병','주로 아래쪽 잎에 테두리가 짙고 가운데가 회색인 작은 둥근 반점이 생깁니다.',
   '잎에 물이 닿지 않게 흙에 물을 주고 통풍을 확보하세요. 병든 잎은 제거하고 다른 잎으로 번지는지 살피세요.',
   'https://www.extension.umd.edu/resource/septoria-leaf-spot-tomatoes'),
 'Bacterial_spot':('세균성 반점병','작고 짙은 반점이 생기며, 주변이 노래지거나 반점이 합쳐질 수 있습니다.',
   '잎을 적시는 물주기를 피하고 젖은 잎을 만지며 작업하지 마세요. 잎 뒷면과 열매의 반점도 확인하세요.',TOMATO_SOURCE),
 'Early_blight':('겹무늬병','아래쪽 잎에서 갈색 반점과 과녁 같은 동심원 무늬가 나타날 수 있습니다.',
   '병든 잎을 정리하고 잎이 오래 젖지 않게 관리하세요. 반점 안에 동심원이 있는지 확인하세요.',TOMATO_SOURCE),
 'Late_blight':('역병','물에 젖은 듯한 짙은 갈색 병반이 잎 가장자리에서 빠르게 커질 수 있습니다.',
   '다른 화분과 떨어뜨려 놓고 잎을 적시지 마세요. 빠르게 번지면 지역 농업기술센터에 사진과 시료 진단을 요청하세요.',TOMATO_SOURCE),
}


def guide_for(row):
    from plant_guidance import GUIDANCE, with_cause
    if row['label'] in GUIDANCE:
        return with_cause(GUIDANCE[row['label']])
    suffix=row['label'].split('___')[1]
    if suffix in GUIDES:
        name,signs,actions,source=GUIDES[suffix]
        causes = {'Septoria_leaf_spot':'셉토리아 곰팡이에 의한 감염 후보입니다.',
                  'Bacterial_spot':'세균에 의한 잎 감염 후보입니다.',
                  'Early_blight':'알터나리아 곰팡이에 의한 감염 후보입니다.',
                  'Late_blight':'역병균에 의한 감염 후보입니다. 젖고 서늘한 환경에서 빠르게 진행할 수 있습니다.'}
        return {'name':name,'features':signs+'\n가능한 원인: '+causes[suffix],
                'actions':actions,'source':source}
    if suffix=='healthy':
        return {'name':'뚜렷한 병징을 찾지 못함','features':'분류기가 학습한 정상 사진과 더 비슷하게 보았습니다.',
                'actions':'누런 부분·갈색 손상·무른 조직이 실제로 있으면 정상 결과를 믿지 말고 그 부분을 가까이 촬영하세요.'}
    return {'name':row['name'],'features':'이 후보의 구별 증상과 관리법은 아직 검토해 등록하지 못했습니다.',
            'actions':'손상 부위 앞뒤를 가까이 촬영하고 변화 속도를 기록하세요. 후보명만으로 약제를 선택하지 마세요.'}


def reconcile_observations(result,observations):
    """A contradictory normal classifier result cannot be the user-facing verdict.

    Do not renormalize remaining diseases or invent a root-rot classifier score.
    """
    result['observed_damage']=sorted(set(observations)&{'brown_necrosis','large_patch','wilting','visible_pests'})
    rows=result.get('candidates',[])
    if result['observed_damage'] and rows and rows[0]['label'].endswith('___healthy'):
        result['normal_result_blocked']=True
        result['comparison_note']='손상 징후가 있어 정상 판정을 취소했습니다. 현재 모델의 학습 범위 밖 원인도 확인해야 합니다.'
    return result


def render_explanation(result):
    if result.get('status')!='experimental_candidates':
        return '병명·장해 후보\n'+result.get('message','분석하지 못했습니다.')
    rows=result['candidates']
    blocked=result.get('normal_result_blocked') or bool(result.get('comparison_note') and rows and rows[0]['label'].endswith('___healthy'))
    if blocked:
        from plant_guidance import blocked_guidance
        guide=blocked_guidance(result.get('species'))
        lines=[guide['name'],'현재 판단: 손상 징후 있음 — 정상으로 판정하지 않습니다.',
               '보이는 상태: '+guide['features'],'가능한 원인: '+guide['cause'],
               '지금 할 일:\n'+guide['actions'],'안내 근거: '+guide['source'],
               '현재 분류기로 원인을 구분하지 못해 병명 순위와 확률은 표시하지 않습니다.']
        return '\n\n'.join(lines)
    top=guide_for(rows[0])
    lines=['병명·장해 후보','가장 유사한 후보: '+top['name'],
           '이 후보의 특징: '+top['features'],'지금 할 일: '+top['actions']]
    if top.get('source'):lines.append('안내 근거: '+top['source'])
    lines.append('다른 후보와 비교 (모델 선호도):\n'+'\n'.join(
        f"{i}. {guide_for(row)['name']} · {row['comparison_score']*100:.1f}%" for i,row in enumerate(rows[:3],1)))
    lines.append('위 %는 학습한 후보끼리의 모델 선호도입니다. 실제로 그 병에 걸렸을 확률은 아직 검증하지 못해 제공하지 않습니다.')
    return '\n\n'.join(lines)
