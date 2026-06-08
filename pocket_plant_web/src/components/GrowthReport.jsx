import React from 'react';
import './GrowthReport.css';
import chartMockup from '../assets/chart.png';

const GrowthReport = () => {
  return (
    <section id="growth-report" className="growth-section">
      <div className="growth-container">

        {/* 상단 뱃지 행 */}
        <div className="growth-badge-row">
          <span className="growth-badge">성장 리포트</span>
          <span className="growth-badge-desc">
            누적된 데이터가 내 식물의 건강 패턴을 알려줍니다.
          </span>
        </div>

        {/* 본문: 텍스트 좌측 + 차트 우측 */}
        <div className="growth-inner">

          {/* 좌측: 텍스트 */}
          <div className="growth-left-content">
            <h2 className="growth-main-title">
              데이터로 보는<br />
              우리 식물의 성장
            </h2>

            <p className="growth-sub-desc">
              매일 수집된 센서 데이터를 주간·월간 리포트로 정리해 보여줍니다.
              바이오 신호 변화, 온습도 패턴, 급수 이력을 한눈에 확인하고,
              AI가 맞춤 케어 팁을 제안합니다.
            </p>

            <ul className="growth-feature-list">
              <li><span className="check-icon">✓</span> 5종 센서 각각의 오늘 수치 + 어제 대비 변화 표시</li>
              <li><span className="check-icon">✓</span> 24시간 · 7일 · 30일 기간별 추이 그래프 전환</li>
              <li><span className="check-icon">✓</span> 적정 · 부족 상태 뱃지로 이상 항목 즉시 파악</li>
              <li><span className="check-icon">✓</span> 바이오 신호 포함 5개 지표 동시 비교 가능</li>
            </ul>
          </div>

          {/* 우측: 차트 이미지 */}
          <div className="growth-right-card">
            <div className="growth-image-wrapper">
              <img
                src={chartMockup}
                alt="식물 센서 데이터 성장 그래프"
                className="growth-chart-img"
              />
            </div>
          </div>

        </div>
      </div>
    </section>
  );
};

export default GrowthReport;