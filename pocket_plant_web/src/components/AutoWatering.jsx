import React from 'react';
import './AutoWatering.css';
import waterMockup from '../assets/water.png';

const AutoWatering = () => {
  return (
    <section id="auto-watering" className="water-section">
      <div className="water-container">

        {/* 상단 뱃지 행 */}
        <div className="water-badge-row">
          <span className="water-badge">자동 물주기</span>
          <span className="water-badge-desc">
            잊어버려도 괜찮아요.<br />
            온라인으로 물을 줄 수 있습니다.
          </span>
        </div>

        {/* 본문: 카드 좌측 + 텍스트 우측 */}
        <div className="water-inner">

          {/* 좌측: UI 카드 이미지 */}
          <div className="water-left-card">
            <div className="water-image-wrapper">
              <img
                src={waterMockup}
                alt="자동 및 수동 물주기 기록 UI 화면"
                className="water-mockup-img"
              />
            </div>
          </div>

          {/* 우측: 텍스트 콘텐츠 */}
          <div className="water-right-content">
            <h2 className="water-main-title">
              과습도, 건조도<br />
              이제 걱정 없어요
            </h2>

            <p className="water-sub-desc">
              토양 습도가 설정한 기준값 아래로 내려가면 릴레이 모듈이
              워터펌프를 자동으로 작동시킵니다. 급수량과 기준값을 직접 설정할 수 있어,
              식물 종류에 맞게 맞춤 관리가 가능합니다.
            </p>

            <ul className="water-feature-list">
              <li><span className="check-icon">✓</span> 수동 급수 버튼으로 즉시 실행 가능</li>
              <li><span className="check-icon">✓</span> 급수 내역과 시간 자동 기록</li>
              <li><span className="check-icon">✓</span> 한 번에 ~~ml 급수</li>
            </ul>
          </div>

        </div>
      </div>
    </section>
  );
};

export default AutoWatering;