import React from 'react';
import './Monitoring.css';

const Monitoring = () => {
  // 우측에 렌더링될 5종 센서 데이터 리스트 배열입니다.
  const sensorData = [
    { icon: "🌱", label: "토양 수분", value: "35", unit: "%", progress: 35, theme: "orange-bar" },
    { icon: "🌡️", label: "온도", value: "22", unit: "°C", progress: 75, theme: "green-bar" },
    { icon: "💧", label: "습도", value: "58", unit: "%", progress: 60, theme: "green-bar" },
    { icon: "☀️", label: "조도", value: "360", unit: "lx", progress: 40, theme: "orange-bar" },
    { icon: "⚡", label: "바이오", value: "360", unit: "lx", progress: 40, theme: "orange-bar" }
  ];

  return (
    <section id="monitoring" className="monitor-section">
      <div className="monitor-container">
        
        {/* 좌측 설명 영역 */}
        <div className="monitor-left-content">
          <div className="monitor-badge-row">
            <span className="monitor-badge">IoT 실시간 모니터링</span>
            <span className="monitor-badge-desc">
              눈으로 볼 수 없는 식물 내부 상태를<br />수치로 정확하게 파악합니다.
            </span>
          </div>

          <h2 className="monitor-main-title">
            화분 속 상태를<br />실시간으로 읽어요
          </h2>

          <p className="monitor-sub-desc">
            ESP32가 화분에 부착된 4종 센서 데이터를 1시간마다 수집해 서버로 전송합니다.<br />
            흙이 촉촉한지, 온도가 적당한지, 햇빛은 충분한지 앱 하나로 한눈에 확인할 수 있어요.
          </p>

          <ul className="monitor-feature-list">
            <li>
              <span className="check-icon">✓</span>
              정전식 토양 습도 센서로 수분 상태 정밀 측정
            </li>
            <li>
              <span className="check-icon">✓</span>
              온습도 센서로 주변 환경 실시간 모니터링
            </li>
            <li>
              <span className="check-icon">✓</span>
              조도 센서로 식물의 일조량 파악
            </li>
            <li>
              <span className="check-icon">✓</span>
              바이오 신호 센서로 식물 생체 전위 측정
            </li>
          </ul>
        </div>

        {/* 우측 실시간 데이터 리스트 영역 */}
        <div className="monitor-right-display">
          {sensorData.map((data, idx) => (
            <div key={idx} className="sensor-card">
              <div className="sensor-info-left">
                <span className="sensor-icon">{data.icon}</span>
                <span className="sensor-label">{data.label}</span>
              </div>
              
              <div className="sensor-info-right">
                <div className="sensor-value-wrap">
                  <span className="sensor-value">{data.value}</span>
                  <span className="sensor-unit">{data.unit}</span>
                </div>
                
                {/* 진행 상태 바 메커니즘 */}
                <div className="progress-bg">
                  <div 
                    className={`progress-fill ${data.theme}`} 
                    style={{ width: `${data.progress}%` }}
                  ></div>
                </div>
              </div>
            </div>
          ))}
        </div>

      </div>
    </section>
  );
};

export default Monitoring;