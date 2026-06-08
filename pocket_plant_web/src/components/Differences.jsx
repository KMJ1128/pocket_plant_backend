import React from 'react';
import './Differences.css';

// 1. Hero.jsx처럼 상단에 assets 폴더의 이미지 4개를 직접 import 합니다.
import df1 from '../assets/df1.png';
import df2 from '../assets/df2.png';
import df3 from '../assets/df3.png';
import df4 from '../assets/df4.png';

const Differences = () => {
  return (
    <section id="differences" className="diff-section">
      <div className="diff-container">
        
        {/* 1. 상단 섹션 타이틀 배지 및 설명 */}
        <div className="diff-header">
          <span className="diff-badge">Differences</span>
          <p className="diff-sub-title">
            기존 서비스는 센서 또는 AI, 하나씩만 제공하지만<br />
            포켓플랜트는 둘 다, 하나의 플랫폼에서 제공합니다.
          </p>
        </div>

        {/* 2. 포켓플랜트 단독 메인 하이라이트 대형 블랙 카드 */}
        <div className="pocket-main-card">
          <div className="pocket-card-header">
            <h3 className="pocket-brand-name">포켓플랜트</h3>
            <span className="our-service-tag">우리 서비스</span>
          </div>
          <p className="pocket-brand-summary">IoT + AI + 자동급수 + 커뮤니티</p>
          
          <div className="pocket-tag-cloud">
            <span className="p-tag">IoT 실시간 센서</span>
            <span className="p-tag">AI 감정 대화</span>
            <span className="p-tag">질병 AI 진단</span>
            <span className="p-tag">바이오 신호 측정</span>
            <span className="p-tag">자동 물주기</span>
            <span className="p-tag">페르소나 설정</span>
            <span className="p-tag">신뢰 기반 커뮤니티</span>
          </div>
        </div>

        {/* 3. 하단 타사 브랜드 리스트 2x2 그리드 */}
        {/* Hero 스타일처럼 배열 .map()을 쓰지 않고, JSX에 이미지 변수를 다이렉트로 매칭해 4개의 카드를 꽂아줍니다. */}
        <div className="brands-grid">
          
          {/* 카드 1: Parrot Flower Power */}
          <div className="brand-card brand-white">
            <div className="brand-card-content">
              <h4 className="brand-card-name">Parrot Flower Power</h4>
              <div className="brand-tag-list">
                <span className="b-feature-tag">토양 습도·온도·조도 센서 + 앱 연동
                  <br /> 수치 기반 모니터링에 강점</span>
              </div>
              <p className="brand-weakness">AI 대화·감정 기능 없음</p>
            </div>
            <div className="brand-graphic-box">
              <img src={df1} alt="Parrot Flower Power 시각화" className="brand-artwork-img" />
            </div>
          </div>

          {/* 카드 2: Planta */}
          <div className="brand-card brand-green">
            <div className="brand-card-content">
              <h4 className="brand-card-name">Planta</h4>
              <div className="brand-tag-list">
                <span className="b-feature-tag">스마트폰 센서로 조도 측정
                  <br /> 종별·환경별 관리 이력 제공</span>
              </div>
              <p className="brand-weakness">실시간 토양·온도 추적 불가</p>
            </div>
            <div className="brand-graphic-box">
              <img src={df2} alt="Planta 시각화" className="brand-artwork-img" />
            </div>
          </div>

          {/* 카드 3: Plantify */}
          <div className="brand-card brand-black">
            <div className="brand-card-content">
              <h4 className="brand-card-name">Plantify</h4>
              <div className="brand-tag-list">
                <span className="b-feature-tag">이미지 기반 병충해 진단과
                  <br />관리 가이드 제공</span>
              </div>
              <p className="brand-weakness">센서 연동·실시간 반영 없음</p>
            </div>
            <div className="brand-graphic-box">
              <img src={df3} alt="Plantify 시각화" className="brand-artwork-img" />
            </div>
          </div>

          {/* 카드 4: Greg */}
          <div className="brand-card brand-light-gray">
            <div className="brand-card-content">
              <h4 className="brand-card-name">Greg</h4>
              <div className="brand-tag-list">
                <span className="b-feature-tag">AI 기반 위치·날씨 데이터 결합한
                  <br /> 맞춤형 물주기 알림</span>
              </div>
              <p className="brand-weakness">실내 센서 없어 상태 오차 있음</p>
            </div>
            <div className="brand-graphic-box">
              <img src={df4} alt="Greg 시각화" className="brand-artwork-img" />
            </div>
          </div>

        </div>

      </div>
    </section>
  );
};

export default Differences;