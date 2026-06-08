import React from 'react';
import './Stats.css';

const Stats = () => {
  return (
    <section className="stats-section">
      <div className="stats-container">
        {/* 카드 1 */}
        <div className="stat-card">
          <div className="stat-value">1,745만</div>
          <div className="stat-label">국내 반려식물 인구</div>
        </div>

        {/* 카드 2 (강조형) */}
        <div className="stat-card highlight-card">
          <div className="stat-value text-green">30.2%</div>
          <div className="stat-label">반려식물 무름병 감소 효과</div>
        </div>

        {/* 카드 3 */}
        <div className="stat-card">
          <div className="stat-value">2.4조</div>
          <div className="stat-label">반려식물 관련 산업 규모</div>
        </div>
      </div>
    </section>
  );
};

export default Stats;