// src/types.ts
export interface ChatMessage {
  chatRoomId: number;
  senderId: number;
  senderNickname: string;
  message: string;
  type: string;
  imageUrl?: string;
  sentAt: string;
}
