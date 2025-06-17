import React, { useEffect, useState } from 'react';
import axios from 'axios';

interface Props {
  token: string;
  onSelectRoom: (roomId: number) => void;
}

const ConsultationRoomList: React.FC<Props> = ({ token, onSelectRoom }) => {
  const [rooms, setRooms] = useState<any[]>([]);

  useEffect(() => {
      if (!token) return; // ✅ 토큰 없으면 요청하지 않음
    axios
      .get('http://localhost:8080/api/v1/consultation-rooms', {
        headers: { Authorization: `Bearer ${token}` },
        withCredentials: true,
      })
      .then((res) => setRooms(res.data))
      .catch((err) => console.error('❌ 목록 조회 실패', err));
  }, [token]);

  return (
    <div>
      <h3>💬 상담 내역</h3>
      <ul>
        {rooms.map((room) => (
          <li
            key={room.consultationRoomId}
            onClick={() => onSelectRoom(room.consultationRoomId)}
          >
            <img src={room.opponentProfileImage} alt="프로필" width={30} />
            <strong>{room.opponentNickname}</strong>
            <div>{room.lastMessage}</div>
            <div>{room.lastMessageAt}</div>
            {room.unreadCount > 0 && (
              <span>🟠 {room.unreadCount}개 안 읽음</span>
            )}
            {room.closed && <span>🔒 종료됨</span>}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default ConsultationRoomList;
