import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import logoImg from '../assets/logo.png';
import './Board.css';

const mockPosts = [];

const Board = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('구매');
  const [searchQuery, setSearchQuery] = useState('');

  const filtered = mockPosts.filter(post =>
    post.title.includes(searchQuery)
  );

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
              <a href="/" className="inactive">홈</a>
              <a href="/board" className="active">게시판</a>
              <a href="#my-plants" className="inactive">내 식물</a>
            </nav>
            <button className="btn-login" onClick={() => navigate('/login')}>로그인</button>
          </div>
        </div>
      </header>

      <div className="board-page">
        <div className="board-container">

          <h2 className="board-title">식물 커뮤니티</h2>
          <p className="board-desc">재배 데이터를 공개해 신뢰도를 높이고, 식물을 사고팔고 소식을 나눠요.</p>

          {/* 탭 */}
          <div className="board-tabs">
            {['구매', '판매'].map(tab => (
              <span
                key={tab}
                className={`board-tab ${activeTab === tab ? 'active' : ''}`}
                onClick={() => setActiveTab(tab)}
              >
                {tab}
              </span>
            ))}
          </div>

          {/* 테이블 헤더 */}
          <div className="board-table-header">
            <span>글 제목</span>
            <span>작성일</span>
          </div>

          {/* 게시글 목록 */}
          <div className="board-list">
            {filtered.length > 0 ? filtered.map(post => (
              <div key={post.id} className="board-item" onClick={() => navigate(`/board/${post.id}`)}>
                <span className="board-item-title">{post.title}</span>
                <span className="board-item-date">{post.date}</span>
              </div>
            )) : (
              <p className="board-empty">게시글이 없습니다.</p>
            )}
          </div>

          {/* 글쓰기 버튼 */}
          <div className="board-actions">
            <button className="write-btn" onClick={() => navigate('/board/write')}>글쓰기</button>
          </div>

          {/* 페이지네이션 */}
          <div className="board-pagination">
            <span className="page-num active">1</span>
          </div>

          {/* 검색 */}
          <div className="board-search">
            <input
              className="search-input"
              type="text"
              placeholder="검색어를 입력해주세요."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
            <button className="search-btn">🔍</button>
          </div>

        </div>
      </div>
    </>
  );
};

export default Board;