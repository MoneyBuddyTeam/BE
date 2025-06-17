import React, { useEffect, useState } from 'react';
import { CompatClient, Stomp } from '@stomp/stompjs';

const ChatTest: React.FC = () => {
  const [client, setClient] = useState<CompatClient | null>(null);
  const [message, setMessage] = useState('');
  const [log, setLog] = useState<string[]>([]);

  useEffect(() => {
    const token = document.cookie
      .split('; ')
      .find(row => row.startsWith('token='))
      ?.split('=')[1];

    const socketUrl = 'ws://localhost:8080/ws-stomp';
    const webSocket = new WebSocket(socketUrl);
    const stompClient = Stomp.over(webSocket);

    stompClient.connect(
      { Authorization: `Bearer ${token}` },
      () => {
        setClient(stompClient);
        stompClient.subscribe('/sub/consultationRoom/1', (msg) => {
          setLog((prev) => [...prev, `[받음] ${msg.body}`]);
        });
      },
      (error: unknown) => {
        console.error('❌ STOMP 연결 실패', error);
      }
    );

    return () => {
      stompClient.disconnect(() => console.log('🛑 연결 해제됨'));
    };
  }, []);

  const sendMessage = () => {
    if (client) {
      client.send('/pub/chat', {}, JSON.stringify({
        consultationRoomId: 1,
        senderId: 1,
        senderNickname: '테스트유저',
        message: message,
        type: 'text',
        imageUrl: null,
        sentAt: new Date().toISOString()
      }));
      setLog((prev) => [...prev, `[보냄] ${message}`]);
      setMessage('');
    }
  };

  return (
    <div>
      <h2>🧪 채팅 테스트</h2>
      <input
        value={message}
        onChange={(e) => setMessage(e.target.value)}
        placeholder="메시지를 입력하세요"
      />
      <button onClick={sendMessage}>보내기</button>
      <div>
        <h4>채팅 로그</h4>
        <ul>
          {log.map((l, idx) => (
            <li key={idx}>{l}</li>
          ))}
        </ul>
      </div>
    </div>
  );
};

export default ChatTest;
