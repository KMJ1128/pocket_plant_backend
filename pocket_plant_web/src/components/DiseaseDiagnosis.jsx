import React from 'react';
import './DiseaseDiagnosis.css';

const DiseaseDiagnosis = () => {
  return (
    <section id="disease-diagnosis" className="disease-section">
      <div className="disease-container">

        {/* 상단 뱃지 행 */}
        <div className="disease-badge-row">
          <span className="disease-badge">AI 질병 진단</span>
          <span className="disease-badge-desc">
            카메라 한 장으로 초보 식집사도<br />
            질병을 빠르게 파악할 수 있어요.
          </span>
        </div>

        {/* 본문: 텍스트 좌측 + 카드 우측 */}
        <div className="disease-inner">

          {/* 좌측: 텍스트 콘텐츠 */}
          <div className="disease-left-content">
            <h2 className="disease-main-title">
              사진 한 장으로<br />
              이상 징후를 잡아요
            </h2>

            <p className="disease-sub-desc">
              잎의 색깔, 반점, 마름 증상을 촬영하면 AI가 질병 가능성을 확률로 분석합니다.
              전문 시직 없이도 초기 대응이 가능하고,
              센서 이력 데이터와 교차 검증해 더 정확한 진단을 제공합니다.
            </p>

            <ul className="disease-feature-list">
              <li><span className="check-icon">✓</span> 이미지 업로드만으로 질병 가능성 확률 제시</li>
              <li><span className="check-icon">✓</span> 잎 곰팡이, 과습, 탄저병 등 주요 질환 감지</li>
              <li><span className="check-icon">✓</span> 센서 이력과 교차 검증해 오진 최소화</li>
              <li><span className="check-icon">✓</span> 진단 결과와 함께 케어 방법 즉시 안내</li>
            </ul>
          </div>

          {/* 우측: 진단 결과 카드 */}
          <div className="disease-right-card">
            <div className="diagnosis-card">
              <div className="diagnosis-card-header">
                <span className="warning-icon">▲</span> 감지된 이상 징후
              </div>

              <div className="diagnosis-item">
                <div className="diagnosis-label-row">
                  <span className="diagnosis-label">잎 끝 마름증</span>
                  <span className="diagnosis-percent high">72%</span>
                </div>
                <div className="diagnosis-bar-bg">
                  <div className="diagnosis-bar-fill high" style={{ width: '72%' }}></div>
                </div>
              </div>

              <div className="diagnosis-item">
                <div className="diagnosis-label-row">
                  <span className="diagnosis-label">과습</span>
                  <span className="diagnosis-percent mid">18%</span>
                </div>
                <div className="diagnosis-bar-bg">
                  <div className="diagnosis-bar-fill mid" style={{ width: '18%' }}></div>
                </div>
              </div>

              <div className="diagnosis-item">
                <div className="diagnosis-label-row">
                  <span className="diagnosis-label">탄저병</span>
                  <span className="diagnosis-percent low">9%</span>
                </div>
                <div className="diagnosis-bar-bg">
                  <div className="diagnosis-bar-fill low" style={{ width: '9%' }}></div>
                </div>
              </div>

              <div className="diagnosis-footer">
                💧 물주기 주기 조정을 권장합니다. 잎 끝이 마르는 현상은 수분 부족이 주원인입니다.
              </div>
            </div>
          </div>

        </div>
      </div>
    </section>
  );
};

export default DiseaseDiagnosis;