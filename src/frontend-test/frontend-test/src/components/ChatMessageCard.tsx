import { format } from 'date-fns';
import type { ConsultationMessage } from '../types/consultation';

interface Props {
  message: ConsultationMessage;
  isMine: boolean;
  isRead?: boolean;
  isLastMine?: boolean;
  isLastMessage?: boolean;
  showMeta: boolean;
  showMenu: boolean;
  onToggleMenu: () => void;
  onDelete?: () => void;
}

function ChatMessageCard({
  message,
  isMine,
  isRead,
  isLastMine,
  isLastMessage,
  showMeta,
  showMenu,
  onToggleMenu,
  onDelete,
}: Props) {
  const fallbackProfileUrl = `https://api.dicebear.com/7.x/identicon/svg?seed=${message.senderId}`;
  const profileImageUrl = message.imageUrl || fallbackProfileUrl;

  return (
    <div className={`flex ${isMine ? 'justify-end' : 'justify-start'} mt-1 mb-1`}>
      <div className={`flex items-end gap-2 max-w-[80%] ${isMine ? 'flex-row-reverse mr-2' : 'ml-2'}`}>
        {showMeta ? (
          <img
            src={profileImageUrl}
            alt="profile"
            className="w-8 h-8 rounded-full border"
          />
        ) : (
          <div className="w-8 h-8" />
        )}

        <div className="flex flex-col items-start gap-1 relative">
          {showMeta && (
            <div className="text-xs text-gray-500 mb-1">
              <span className="font-semibold">{message.senderNickname}</span> ·{' '}
              {format(new Date(message.sentAt), 'HH:mm')}
            </div>
          )}

          <div
            className={`relative px-4 py-2 text-sm rounded-xl shadow-md whitespace-pre-wrap break-words cursor-pointer ${
              isMine
                ? 'bg-blue-100 text-right self-end rounded-br-none'
                : 'bg-white text-left self-start rounded-bl-none'
            }`}
            onClick={onToggleMenu}
          >
            {message.type === 'IMAGE' ? (
              <img src={message.imageUrl ?? ''} alt="보낸 이미지" className="max-w-xs max-h-40 rounded" />
            ) : (
              message.message
            )}
            <div
              className={`absolute bottom-0 w-2 h-2 bg-inherit ${
                isMine
                  ? 'right-0 rotate-45 translate-x-1 translate-y-1'
                  : 'left-0 rotate-45 -translate-x-1 translate-y-1'
              }`}
            />
          </div>

          {isMine && isLastMine && isLastMessage && (
            <div className="text-[10px] text-gray-400 self-end mt-0.5">
              {isRead ? '읽음' : '전송됨'}
            </div>
          )}

          {showMenu && (
            <div className="absolute top-full mt-1 z-10 right-0 bg-white border rounded shadow text-xs w-[75px]">
              <button
                className="px-3 py-1 hover:bg-gray-100 w-full text-left"
                onClick={() => {
                  navigator.clipboard.writeText(message.message);
                  onToggleMenu();
                }}
              >
                복사하기
              </button>
              {isMine && (
                <button
                  className="px-3 py-1 text-red-500 hover:bg-red-100 w-full text-left"
                  onClick={() => {
                    onDelete?.();
                    onToggleMenu();
                  }}
                >
                  삭제하기
                </button>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default ChatMessageCard;
