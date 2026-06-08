import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import logoImg from '../assets/logo.png';
import './ForgotPassword.css';

const ForgotPassword = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [sent, setSent] = useState(false);

  const isValidEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

  const handleSubmit = () => {
    if (!isValidEmail(email)) {
      alert('올바른 이메일 형식을 입력해주세요.');
      return;
    }
    setSent(true);
  };

  return (
    <>
      <header className="main-header">
        <div className="header-inner">
          <div className="header-logo-wrap" onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>
            <img src={logoImg} alt="Pocket Plants 로고" className="logo-img" />
            <span>Pocket Plants</span>
          </div>
          <div className="header-right">
            <nav className="header-nav">
              <a href="/" className="active">홈</a>
              <a href="#board" className="inactive">게시판</a>
              <a href="#my-plants" className="inactive">내 식물</a>
            </nav>
            <button className="btn-login" onClick={() => navigate('/login')}>로그인</button>
          </div>
        </div>
      </header>

      <div className="forgot-page">
        <div className="forgot-box">
          <h2 className="forgot-title">비밀번호 찾기</h2>
          <p className="forgot-desc">가입한 이메일 주소로 인증 메일을 보내드립니다.</p>

          <input
            className={`forgot-input ${email.length > 0 && !isValidEmail(email) ? 'input-error' : ''}`}
            type="text"
            placeholder="아이디 (이메일)"
            value={email}
            onChange={(e) => { setEmail(e.target.value); setSent(false); }}
          />
          {email.length > 0 && !isValidEmail(email) && (
            <p className="input-error-msg">올바른 이메일 형식이 아닙니다.</p>
          )}
          {sent && (
            <p className="input-success-msg">인증 메일이 발송되었습니다.</p>
          )}

          <button className="forgot-submit-btn" onClick={handleSubmit}>
            인증 메일 받기
          </button>
        </div>
      </div>
    </>
  );
};

export default ForgotPassword;