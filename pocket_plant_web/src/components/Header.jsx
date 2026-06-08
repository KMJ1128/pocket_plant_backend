import React from 'react';
import { useNavigate } from 'react-router-dom';
import logoImg from '../assets/logo.png';
import './Header.css';

const Header = () => {
  const navigate = useNavigate();

  return (
    <header className="main-header">
      <div className="header-inner">
        <div className="header-logo-wrap">
          <img src={logoImg} alt="Pocket Plants 로고" className="logo-img" />
          <span>Pocket Plants</span>
        </div>

        <div className="header-right">
          <nav className="header-nav">
            <a href="#home" className="active">홈</a>
            <a href="/board" className="inactive">게시판</a>
            <a href="#my-plants" className="inactive">내 식물</a>
          </nav>
          <button className="btn-login" onClick={() => navigate('/login')}>로그인</button>
        </div>
      </div>
    </header>
  );
};

export default Header;