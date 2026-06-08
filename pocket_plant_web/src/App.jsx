import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import Hero from './components/Hero';
import Stats from './components/Stats';
import Services from './components/Services';
import Differences from './components/Differences';
import Monitoring from './components/Monitoring';
import EmotionalCare from './components/EmotionalCare';
import DiseaseDiagnosis from './components/DiseaseDiagnosis';
import AutoWatering from './components/AutoWatering';
import GrowthReport from './components/GrowthReport';
import Community from './components/Community';
import Login from './components/Login';
import Signup from './components/Signup';
import ForgotPassword from './components/ForgotPassword';
import ResetPassword from './components/ResetPassword';
import Board from './components/Board';
import BoardWrite from './components/BoardWrite';

function MainPage() {
  return (
    <>
      <Header />
      <main>
        <Hero />
        <Stats />
        <Services />
        <Differences />
        <Monitoring />
        <EmotionalCare />
        <DiseaseDiagnosis />
        <AutoWatering />
        <GrowthReport />
        <Community />
      </main>
    </>
  );
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/board" element={<Board />} />
        <Route path="/board/write" element={<BoardWrite />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;