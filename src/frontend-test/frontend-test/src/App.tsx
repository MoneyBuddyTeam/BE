import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import React, { useState } from 'react';
import LoginForm from './components/LoginForm';
import AdvisorDetailPage from './pages/AdvisorDetailPage';
import ConsultationRoomList from './components/ConsultationRoomList';
import ConsultationRoomPage from './pages/ConsultationRoomPage'; // 👈 추가

function App() {
  const [token, setToken] = useState('');
  const [nickname, setNickname] = useState('');
  const [userId, setUserId] = useState<number | null>(null);
  const [roomId, setRoomId] = useState<number | null>(null);

  return (
    <Router>
      <div className="App">
        {!token ? (
          <LoginForm
            setToken={setToken}
            setNickname={setNickname}
            setUserId={setUserId}
          />
        ) : (
          <Routes>
            <Route path="/" element={<Navigate to="/advisors/1" />} />

            <Route
              path="/advisors/:id"
              element={
                <AdvisorDetailPage
                  token={token}
                  clientId={userId}
                  onRoomCreated={setRoomId}
                />
              }
            />

            <Route
              path="/consultation/rooms"
              element={
                <ConsultationRoomList
                  token={token}
                  onSelectRoom={setRoomId}
                />
              }
            />

            {/* ✅ 새로 추가된 ConsultationRoomPage 라우트 */}
            <Route
              path="/consultation/rooms/:roomId"
              element={<ConsultationRoomPage />}
            />
          </Routes>
        )}
      </div>
    </Router>
  );
}

export default App;
