// ✅ ChatBox.tsx 리팩터링 최종본
import { useEffect, useRef, useState, useMemo } from 'react';
import axios from 'axios';
import { format, isToday, isYesterday } from 'date-fns';
import ChatMessageCard from './ChatMessageCard';
import type { ConsultationMessage } from '../types/consultation';

interface ChatBoxProps {
  roomId: string;
  messages: ConsultationMessage[];
  setMessages: React.Dispatch<React.SetStateAction<ConsultationMessage[]>>;
  myId: number;
}

const getDateLabel = (dateStr: string) => {
  const date = new Date(dateStr);
  if (isToday(date)) return '오늘';
  if (isYesterday(date)) return '어제';
  return format(date, 'yyyy년 M월 d일 (EEE)');
};

function ChatBox({ roomId, messages, setMessages, myId }: ChatBoxProps) {
  const [page, setPage] = useState(0);
  const [last, setLast] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  const prevScrollHeightRef = useRef<number>(0);
  const [activeMenuId, setActiveMenuId] = useState<number | null>(null);
  const [hasMarkedAsRead, setHasMarkedAsRead] = useState(false); // ✅ 무한루프 방지

  // ✅ 새로운 채팅방에 들어올 때 읽음 상태 초기화
  useEffect(() => {
    setHasMarkedAsRead(false);
  }, [roomId]);

  const lastMyMessageId = useMemo(() => {
    const reversed = [...messages].reverse();
    const lastMine = reversed.find((msg) => msg.senderId === myId);
    return lastMine?.messageId;
  }, [messages, myId]);

  const loadMessages = async (pageToLoad: number) => {
    const res = await axios.get(`/api/v1/consultation/rooms/${roomId}/messages`, {
      params: { page: pageToLoad, size: 20, sort: 'sentAt,desc' },
      withCredentials: true,
    });
    const reversed = res.data.content.reverse();
    setMessages((prev) => [...reversed, ...prev]);
    setLast(res.data.last);
  };

  // ✅ 초기 메시지 로딩
  useEffect(() => {
    loadMessages(0);
  }, [roomId]);

  // ✅ 읽음 처리 (내가 상대 메시지를 읽었을 경우)
  useEffect(() => {
    if (messages.length === 0 || hasMarkedAsRead) return;

    const lastReceived = [...messages].reverse().find((m) => m.senderId !== myId);
    if (!lastReceived) return;

    axios
      .patch(
        `/api/v1/consultation/rooms/${roomId}/read`,
        { messageId: lastReceived.messageId },
        { withCredentials: true }
      )
      .then(() => {
        setMessages((prev) =>
          prev.map((msg) =>
            msg.senderId === myId ? { ...msg, isReadByReceiver: true } : msg
          )
        );
        setHasMarkedAsRead(true); // ✅ 중복 요청 방지
      })
      .catch(console.error);

    console.log(`/api/v1/consultation/rooms/${roomId}/read 엔드포인트 실행`);
  }, [messages, myId, roomId, hasMarkedAsRead, setMessages]);

  const handleScroll = () => {
    const el = containerRef.current;
    if (!el || el.scrollTop > 10 || last) return;

    prevScrollHeightRef.current = el.scrollHeight;
    const nextPage = page + 1;

    loadMessages(nextPage).then(() => {
      setPage(nextPage);
      setTimeout(() => {
        if (el) {
          const newHeight = el.scrollHeight;
          el.scrollTop = newHeight - prevScrollHeightRef.current;
        }
      }, 50);
    });
  };

  return (
    <div
      id="consultation-messages"
      ref={containerRef}
      className="flex flex-col gap-2 overflow-y-auto p-4 bg-gray-50"
      onScroll={handleScroll}
    >
      {messages.map((msg, idx) => {
        const isMine = msg.senderId === myId;
        const prev = messages[idx - 1];
        const showMeta = !prev || msg.senderId !== prev.senderId;
        const showDateDivider =
          !prev || format(new Date(msg.sentAt!), 'yyyy-MM-dd') !== format(new Date(prev.sentAt!), 'yyyy-MM-dd');
        const isLastMessage = idx === messages.length - 1;

        return (
          <div key={`msg-${msg.messageId || idx}`}>
            {showDateDivider && (
              <div className="flex items-center justify-center my-4">
                <div className="flex-grow border-t border-gray-300 mx-2" />
                <span className="text-xs text-gray-500">{getDateLabel(msg.sentAt!)}</span>
                <div className="flex-grow border-t border-gray-300 mx-2" />
              </div>
            )}
            <ChatMessageCard
              message={msg}
              isMine={isMine}
              isRead={msg.isReadByReceiver}
              isLastMine={msg.messageId === lastMyMessageId}
              isLastMessage={isLastMessage}
              showMeta={showMeta}
              showMenu={activeMenuId === idx}
              onToggleMenu={() => setActiveMenuId(activeMenuId === idx ? null : idx)}
              onDelete={() => alert('삭제는 구현 예정')}
            />
          </div>
        );
      })}
    </div>
  );
}

export default ChatBox;
