import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import logoImg from '../assets/logo.png';
import './BoardWrite.css';

const BoardWrite = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('구매');
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [file, setFile] = useState(null);

  const handleSave = () => {
    if (!title.trim()) {
      alert('제목을 입력해주세요.');
      return;
    }
    if (!content.trim()) {
      alert('내용을 입력해주세요.');
      return;
    }
    alert('저장되었습니다.');
    navigate('/board');
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
              <a href="/" className="inactive">홈</a>
              <a href="/board" className="active">게시판</a>
              <a href="#my-plants" className="inactive">내 식물</a>
            </nav>
            <button className="btn-login" onClick={() => navigate('/login')}>로그인</button>
          </div>
        </div>
      </header>

      <div className="write-page">
        <div className="write-container">

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

          {/* 폼 테이블 */}
          <div className="write-table">

            <div className="write-row">
              <span className="write-label">제목</span>
              <input
                className="write-input"
                type="text"
                placeholder="제목을 입력해주세요."
                value={title}
                onChange={(e) => setTitle(e.target.value)}
              />
            </div>

            <div className="write-row">
              <span className="write-label">내용</span>
              <textarea
                className="write-textarea"
                placeholder="내용을 입력해주세요."
                value={content}
                onChange={(e) => setContent(e.target.value)}
              />
            </div>

            <div className="write-row">
              <span className="write-label">첨부파일</span>
              <div className="write-file">
                <label className="file-btn">
                  파일 선택
                  <input
                    type="file"
                    style={{ display: 'none' }}
                    onChange={(e) => setFile(e.target.files[0])}
                  />
                </label>
                {file && <span className="file-name">{file.name}</span>}
              </div>
            </div>

          </div>

          {/* 저장 버튼 */}
          <div className="write-actions">
            <button className="save-btn" onClick={handleSave}>저장</button>
          </div>

        </div>
      </div>
    </>
  );
};

export default BoardWrite;