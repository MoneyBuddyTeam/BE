// ✅ ConsultationRoomPage.tsx 리팩터링
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import { connectStomp, disconnectStomp, sendMessage } from '../utils/stompClient';
import ChatBox from '../components/ChatBox';
import ChatInput from '../components/ChatInput';
import type { ConsultationMessage } from '../types/consultation';

function ConsultationRoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const [messages, setMessages] = useState<ConsultationMessage[]>([]);

  const senderId = Number(localStorage.getItem('userId'));
  const senderNickname = localStorage.getItem('nickname') || '알 수 없음';

  useEffect(() => {
    if (!roomId) return;

    connectStomp({
      roomId,
      token: '', // 필요한 경우 JWT 토큰 설정
      onMessage: (msg: ConsultationMessage) => {
        setMessages((prev) => [...prev, msg]);
      },
    });

    return () => disconnectStomp();
  }, [roomId]);

  const handleSend = async (text: string, imageFile?: File) => {
    if (!roomId) return;

    if (imageFile) {
      const formData = new FormData();
      formData.append('image', imageFile);
      formData.append('content', text);
      formData.append('roomId', roomId);
      formData.append('senderId', String(senderId));
      formData.append('senderNickname', senderNickname);

      await axios.post('/api/v1/consultation/messages/image', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        withCredentials: true,
      });
    } else {
      sendMessage({
        roomId,
        message: {
          senderId,
          senderNickname,
          content: text,
          contentType: 'TEXT',
          sentAt: new Date(Date.now() + 9 * 60 * 60 * 1000).toISOString(),
        },
      });
    }
  };

  return (
    <div className="flex flex-col h-screen max-w-2xl mx-auto">
      <h1 className="text-lg font-bold px-4 py-2 border-b">채팅방 #{roomId}</h1>

      <div className="flex-1 overflow-y-auto">
        {roomId && (
          <ChatBox
            roomId={roomId}
            messages={messages}
            setMessages={setMessages}
            myId={senderId}
          />
        )}
      </div>

      <ChatInput onSend={handleSend} />
    </div>
  );
}

export default ConsultationRoomPage;
