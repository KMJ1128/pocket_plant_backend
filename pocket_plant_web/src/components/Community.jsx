import React from 'react';
import './Community.css';

const Community = () => {
  return (
    <section id="community" className="community-section">
      <div className="community-container">

        {/* 상단 뱃지 행 */}
        <div className="community-badge-row">
          <span className="community-badge">신뢰 기반 커뮤니티</span>
          <span className="community-badge-desc">
            재배 데이터를 공개해 신뢰도를 높이고,<br />
            식물을 사고팔고 소식을 나눠요.
          </span>
        </div>

        {/* 본문: 좌측 커뮤니티 UI + 우측 텍스트 */}
        <div className="community-inner">

          {/* 좌측: 커뮤니티 UI 목업 */}
          <div className="community-left-card">
            <div className="community-mockup">
              <h3 className="mockup-title">식물 커뮤니티</h3>
              <p className="mockup-subtitle">재배 데이터를 공개해 신뢰도를 높이고, 식물을 사고팔고 소식을 나눠요.</p>

              <div className="mockup-tabs">
                <span className="mockup-tab active">구매</span>
                <span className="mockup-divider">|</span>
                <span className="mockup-tab">판매</span>
              </div>

              <div className="mockup-input">글 제목</div>

              <div className="mockup-list">
                <div className="mockup-item">식물 사ㅏ봅니다~~</div>
                <div className="mockup-item">식물 사ㅏ봅니다~~</div>
                <div className="mockup-item">식물 사ㅏ봅니다~~</div>
              </div>
            </div>
          </div>

          {/* 우측: 텍스트 콘텐츠 */}
          <div className="community-right-content">
            <h2 className="community-main-title">
              데이터가<br />
              신뢰를 증명해요
            </h2>

            <p className="community-sub-desc">
              식물을 판매할 때 실제 센서 데이터를 함께 공개할 수 있습니다.
              구매자는 데이터를 보고 건강 상태를 직접 확인할 수 있어,
              단순 사진보다 훨씬 투명한 거래가 가능합니다.
            </p>

            <ul className="community-feature-list">
              <li><span className="check-icon">✓</span> 구매·판매·소식 탭으로 나뉜 커뮤니티 게시판</li>
              <li><span className="check-icon">✓</span> 센서 데이터 공개로 신뢰도 상승</li>
              <li><span className="check-icon">✓</span> 투명한 거래</li>
            </ul>
          </div>

        </div>
      </div>
    </section>
  );
};

export default Community;