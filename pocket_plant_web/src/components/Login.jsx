import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Login.css';

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  return (
    <>
      <header className="main-header">
        <div className="header-inner">
          <div className="header-logo-wrap" onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>
            <span className="logo-emoji">🌵</span>
            <span>Pocket Plants</span>
          </div>
          <div className="header-right">
            <nav className="header-nav">
              <a href="/" className="inactive">홈</a>
              <a href="#board" className="inactive">게시판</a>
              <a href="#my-plants" className="inactive">내 식물</a>
            </nav>
            <button className="btn-login" onClick={() => navigate('/login')}>로그인</button>
          </div>
        </div>
      </header>

      <div className="login-page">
        <div className="login-box">
          <h2 className="login-title">로그인</h2>
          <p className="login-desc">
            로그인 후 간단한 계정전환 절차만 거치면<br />
            모든 Pocket Plants 서비스를 즐길 수 있습니다.
          </p>

          <input
            className="login-input"
            type="text"
            placeholder="아이디 (이메일)"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <input
            className="login-input"
            type="password"
            placeholder="비밀번호"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          <button className="login-submit-btn">로그인</button>

          <div className="login-links">
            <span onClick={() => navigate('/signup')} style={{ cursor: 'pointer' }}>회원가입</span>
            <span onClick={() => navigate('/forgot-password')} style={{ cursor: 'pointer' }}>비밀번호 찾기</span>
          </div>
        </div>
      </div>
    </>
  );
};

export default Login;