import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import logoImg from '../assets/logo.png';
import './ResetPassword.css';

const ResetPassword = () => {
  const navigate = useNavigate();
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');

  const isValidPassword = (pw) => /^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,15}$/.test(pw);
  const passwordMismatch = passwordConfirm.length > 0 && password !== passwordConfirm;
  const passwordMatch = passwordConfirm.length > 0 && password === passwordConfirm;

  const handleSubmit = () => {
    if (!isValidPassword(password)) {
      alert('비밀번호 조건을 확인해주세요.');
      return;
    }
    if (passwordMismatch) {
      alert('비밀번호가 일치하지 않습니다.');
      return;
    }
    alert('비밀번호가 변경되었습니다.');
    navigate('/login');
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

      <div className="reset-page">
        <div className="reset-box">
          <h2 className="reset-title">비밀번호 변경하기</h2>
          <p className="reset-desc">가입한 이메일 주소로 인증 메일을 보내드립니다.</p>

          {/* 새 비밀번호 */}
          <input
            className={`reset-input ${password.length > 0 && !isValidPassword(password) ? 'input-error' : ''}`}
            type="password"
            placeholder="새 비밀번호"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          {password.length > 0 && !isValidPassword(password) ? (
            <p className="input-error-msg">8~15자리 숫자, 영문, 특수기호를 모두 포함해야 합니다.</p>
          ) : (
            <p className="input-hint">8~15자리 숫자, 영문, 특수기호 사용</p>
          )}

          {/* 새 비밀번호 확인 */}
          <input
            className={`reset-input ${passwordMismatch ? 'input-error' : passwordMatch ? 'input-success' : ''}`}
            type="password"
            placeholder="새 비밀번호 확인"
            value={passwordConfirm}
            onChange={(e) => setPasswordConfirm(e.target.value)}
          />
          {passwordMismatch && <p className="input-error-msg">비밀번호가 일치하지 않습니다.</p>}
          {passwordMatch && <p className="input-success-msg">비밀번호가 일치합니다.</p>}

          <button className="reset-submit-btn" onClick={handleSubmit}>변경하기</button>
        </div>
      </div>
    </>
  );
};

export default ResetPassword;