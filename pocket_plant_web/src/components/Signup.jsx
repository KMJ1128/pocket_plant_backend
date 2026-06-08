import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import logoImg from '../assets/logo.png';
import './Signup.css';

const Signup = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [authCode, setAuthCode] = useState('');
  const [authSent, setAuthSent] = useState(false);
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [name, setName] = useState('');

  const passwordMismatch = passwordConfirm.length > 0 && password !== passwordConfirm;
  const passwordMatch = passwordConfirm.length > 0 && password === passwordConfirm;

  const isValidEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  const isValidPassword = (pw) => /^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,15}$/.test(pw);

  const handleAuthClick = () => {
    if (!isValidEmail(email)) {
      alert('올바른 이메일 형식을 입력해주세요.');
      return;
    }
    setAuthSent(true);
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

      <div className="signup-page">
        <div className="signup-box">
          <h2 className="signup-title">회원가입</h2>
          <p className="signup-desc">회원정보를 입력해주세요</p>

          {/* 이메일 + 인증하기 */}
          <div className="signup-input-row">
            <input
              className={`signup-input ${email.length > 0 && !isValidEmail(email) ? 'input-error' : ''}`}
              type="text"
              placeholder="아이디 (이메일)"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
            <button className={`auth-btn ${authSent ? 'auth-sent' : ''}`} onClick={handleAuthClick}>
              {authSent ? '재발송' : '인증하기'}
            </button>
          </div>
          {email.length > 0 && !isValidEmail(email) && (
            <p className="input-error-msg">올바른 이메일 형식이 아닙니다.</p>
          )}
          {authSent && (
            <p className="input-success-msg">인증번호가 발송되었습니다.</p>
          )}

          {/* 인증번호 */}
          <input
            className="signup-input full"
            type="text"
            placeholder="인증번호"
            value={authCode}
            onChange={(e) => setAuthCode(e.target.value)}
          />

          {/* 비밀번호 */}
          <input
            className={`signup-input full ${password.length > 0 && !isValidPassword(password) ? 'input-error' : ''}`}
            type="password"
            placeholder="비밀번호"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          {password.length > 0 && !isValidPassword(password) ? (
            <p className="input-error-msg">8~15자리 숫자, 영문, 특수기호를 모두 포함해야 합니다.</p>
          ) : (
            <p className="input-hint">8~15자리 숫자, 영문, 특수기호 사용</p>
          )}

          {/* 비밀번호 확인 */}
          <input
            className={`signup-input full ${passwordMismatch ? 'input-error' : passwordMatch ? 'input-success' : ''}`}
            type="password"
            placeholder="비밀번호 확인"
            value={passwordConfirm}
            onChange={(e) => setPasswordConfirm(e.target.value)}
          />
          {passwordMismatch && <p className="input-error-msg">비밀번호가 일치하지 않습니다.</p>}
          {passwordMatch && <p className="input-success-msg">비밀번호가 일치합니다.</p>}

          {/* 이름 */}
          <input
            className="signup-input full"
            type="text"
            placeholder="이름"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />

          <button className="signup-submit-btn">회원가입하기</button>
        </div>
      </div>
    </>
  );
};

export default Signup;