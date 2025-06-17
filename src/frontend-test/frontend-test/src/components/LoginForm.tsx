import React, { useState } from 'react';
import axios from 'axios';

const LoginForm = ({
  setToken,
  setNickname,
  setUserId,
}: {
  setToken: (token: string) => void;
  setNickname: (nickname: string) => void;
  setUserId: (userId: number) => void;
}) => {
  const [email, setEmail] = useState('client@example.com');
  const [password, setPassword] = useState('password1234!');

const handleLogin = async () => {
  try {
    const response = await axios.post(
      'http://localhost:8080/api/v1/users/login',
      {
        email,
        password,
      },
      {
        withCredentials: true,
      }
    );

    const { token, nickname, userId } = response.data;
    console.log('✅ 로그인 성공:', token);

    // ✅ 상태 업데이트
    setToken(token);
    setNickname(nickname);
    setUserId(userId);

    // ✅ 로컬스토리지에도 저장
    localStorage.setItem('token', token);
    localStorage.setItem('userId', String(userId));
    localStorage.setItem('nickname', nickname); // 필요 시
  } catch (error) {
    console.error('❌ 로그인 실패:', error);
  }
};


  return (
    <div>
      <h2>테스트용 로그인</h2>
      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="이메일"
      />
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="비밀번호"
      />
      <button onClick={handleLogin}>로그인</button>
    </div>
  );
};

export default LoginForm;
