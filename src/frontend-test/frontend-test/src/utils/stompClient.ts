import { CompatClient, Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

let stompClient: CompatClient | null = null;

interface ConnectStompArgs {
  roomId: number | string;
  token: string;
  onMessage: (msg: any) => void;
}

export const connectStomp = ({ roomId, token, onMessage }: ConnectStompArgs) => {
  stompClient = Stomp.over(() => new SockJS('http://localhost:8080/ws-stomp'));
  stompClient.reconnectDelay = 5000;

  stompClient.connect(
    {},
    () => {
      console.log('✅ STOMP 연결됨');
      stompClient?.subscribe(`/sub/chat/room/${roomId}`, (msg) => {
        const body = JSON.parse(msg.body);
        onMessage(body);
      });
    },
    (error: unknown) => {
      console.error('❌ WebSocket 연결 실패:', error);
    }
  );
};

interface SendMessageArgs {
  roomId: number | string;
  message: {
    senderId: number;
    senderNickname: string;
    content: string;
    contentType: 'TEXT' | 'IMAGE';
    sentAt?: string;
    imageUrl?: string | null;
  };
}

export const sendMessage = ({ roomId, message }: SendMessageArgs) => {
  if (!stompClient || !stompClient.connected) {
    console.error('❗️STOMP 클라이언트가 연결되지 않았습니다.');
    return;
  }

  const payload = {
    consultationRoomId: Number(roomId),
    senderId: message.senderId,
    senderNickname: message.senderNickname,
    message: message.content,
    type: message.contentType,
    imageUrl: message.imageUrl ?? null,
    sentAt: message.sentAt ?? new Date().toISOString(),
  };

  stompClient.send('/pub/chat', {}, JSON.stringify(payload));
};

export const disconnectStomp = () => {
  stompClient?.disconnect(() => {
    console.log('🔌 WebSocket 연결 종료');
  });
};
