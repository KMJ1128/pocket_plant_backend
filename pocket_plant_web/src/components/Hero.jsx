import React from 'react';
import './Hero.css';
import heroImg from '../assets/hero.png';

const Hero = () => {
  return (
    <section className="hero-section">
      <div className="hero-container">
        {/* 좌측 텍스트 영역 */}
        <div className="hero-text">
          <h1 className="hero-main-title">
            식물이 <br />
            <span className="highlight">말을 걸어요,</span> <br />
            당신에게.
          </h1>
          <p className="hero-sub-desc">
            IoT 센서가 화분 데이터 환경을 실시간 수집하고, <br />
            AI가 식물에 고유한 캐릭터와 감성을 부여합니다. <br />
            식물과 자연스럽게 대화하고, 이미지로 질병을 조기 진단하며, <br />
            자동 물주기 시스템으로 반려식물 죽음을 방지할 수 있습니다.
          </p>
          <button className="btn-start">시작하기</button>
        </div>

        {/* 우측 이미지 영역 */}
        <div className="hero-image-area">
          <div className="dashed-circle"></div>
          <div className="hero-image-wrapper">
            <img src={heroImg} alt="Pocket Plants Main" className="hero-main-img" />
          </div>
        </div>
      </div>
    </section>
  );
};

export default Hero;