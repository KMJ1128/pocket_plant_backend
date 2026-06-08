import React from 'react';
import './EmotionalCare.css';
import chatMockup from '../assets/chat.png';

const EmotionalCare = () => {
  return (
    <section id="emotional-care" className="emotion-section">
      <div className="emotion-container">

        {/* 상단 뱃지 행 */}
        <div className="emotion-badge-row">
          <span className="emotion-badge">AI 감정 대화</span>
          <span className="emotion-badge-desc">
            식물이 직접 말을 건넵니다.<br />
            센서 데이터가 감정이 되는 순간.
          </span>
        </div>

        {/* 본문: 카드 좌측 + 텍스트 우측 */}
        <div className="emotion-inner">

          {/* 좌측: 대화창 이미지 */}
          <div className="emotion-left-card">
            <div className="chat-image-wrapper">
              <img
                src={chatMockup}
                alt="식물과의 AI 감정 대화창 메신저 화면"
                className="chat-mockup-img"
              />
            </div>
          </div>

          {/* 우측: 텍스트 콘텐츠 */}
          <div className="emotion-right-content">
            <h2 className="emotion-main-title">
              내 식물만의<br />
              고유한 목소리
            </h2>

            <p className="emotion-sub-desc">
              LLM이 실시간 센서 데이터를 분석해 식물의 현재 상태와 감정을 생성합니다.
              설정한 페르소나(츤데레, 호기심형 등)에 따라 말투와 성격이 달라져,
              식물마다 완전히 다른 캐릭터가 됩니다.
            </p>

            <ul className="emotion-feature-list">
              <li><span className="check-icon">✓</span> 센서 수치를 반영한 상황별 맞춤 대사 생성</li>
              <li><span className="check-icon">✓</span> 츤데레, 선비형, 호기심형 등 다양한 페르소나 선택</li>
              <li><span className="check-icon">✓</span> 물 부족, 과습, 햇빛 부족 등 이상 상태 자연스럽게 표현</li>
              <li><span className="check-icon">✓</span> LCD 디스플레이로 화분에서도 감정 상태 확인</li>
            </ul>
          </div>

        </div>
      </div>
    </section>
  );
};

export default EmotionalCare;