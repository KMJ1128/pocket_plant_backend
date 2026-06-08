import React from 'react';
import './Services.css';

const Services = () => {
  const serviceData = [
    { id: 1, emoji: '🌡️', tag: '주요 기능', title: 'IoT 실시간 모니터링', desc: '토양수분·온도·조도·바이오신호를 ESP32로 실시간 수집. 수치로 정확하게 식물 상태를 파악합니다.', targetId: 'monitoring' },
    { id: 2, emoji: '🤖', tag: '주요 기능', title: 'AI 감정 대화', desc: '센서 데이터를 기반으로 식물 고유의 캐릭터와 감정이 생성됩니다. 앱을 통해 자연스럽게 대화하세요.', targetId: 'emotional-care' },
    { id: 3, emoji: '🔬', tag: '주요 기능', title: 'AI 질병 진단', desc: '카메라로 잎을 촬영하면 AI가 질병 가능성을 확률로 제시합니다. 초보 식집사도 빠르게 대응할 수 있어요.', targetId: 'disease-diagnosis' },
    { id: 4, emoji: '💧', tag: '주요 기능', title: '자동 물주기', desc: '토양 습도 데이터를 기반으로 릴레이 모듈이 워터펌프를 자동 제어합니다. 과습·건조 걱정을 줄일 수 있습니다.', targetId: 'auto-watering' },
    { id: 5, emoji: '📊', tag: '주요 기능', title: '성장 리포트', desc: '바이오 신호·온습도·조도 변화를 시계열 그래프로 확인할 수 있습니다. 누적 데이터로 맞춤 케어를 제공합니다.', targetId: 'growth-report' },
    { id: 6, emoji: '🛒', tag: '주요 기능', title: '신뢰 기반 커뮤니티', desc: '실제 재배 데이터를 공유하며 식물을 사고팔 수 있는 투명한 마켓플레이스를 제공합니다. 데이터가 신뢰를 증명합니다.', targetId: 'community' }
  ];

  const scrollToSection = (targetId) => {
    const el = document.getElementById(targetId);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <section className="services-section">
      <div className="services-container">
        
        <div className="services-header">
          <span className="services-badge">Services</span>
          <p className="services-sub-title">
            IoT가 식물의 상태를 읽고,<br />
            AI가 감정을 불어넣어 당신과 교감합니다
          </p>
        </div>

        <div className="services-grid">
          {serviceData.map((item) => (
            <div key={item.id} className="service-card">
              
              <div className="card-top-layout">
                <div className="clover-icon-box">
                  <span className="service-emoji">{item.emoji}</span>
                </div>
                
                <div className="card-title-group">
                  <span className="sub-tag">{item.tag}</span>
                  <h3 className="main-feature-title">{item.title}</h3>
                </div>

                <button
                  className="card-arrow-btn"
                  onClick={() => scrollToSection(item.targetId)}
                >
                  <span className="arrow-icon">→</span>
                </button>
              </div>

              <hr className="card-divider" />
              <p className="card-description">{item.desc}</p>
              
            </div>
          ))}
        </div>

      </div>
    </section>
  );
};

export default Services;