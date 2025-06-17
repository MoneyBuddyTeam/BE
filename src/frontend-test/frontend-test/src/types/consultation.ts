export interface ConsultationMessage {
  messageId: number;
  consultationRoomId: number;
  senderId: number;
  senderNickname: string;
  message: string;
  type: 'TEXT' | 'IMAGE' | 'SYSTEM';
  imageUrl?: string | null;
  sentAt: string;
  isReadByReceiver: boolean;
}
