// src/services/sendChatMessage.ts
import { ChatMessage } from "../types";
import { stompClient } from "./connectChatSocket";

export const sendChatMessage = (message: ChatMessage) => {
  if (!stompClient || !stompClient.connected) {
    console.warn("❌ STOMP 미연결 상태");
    return;
  }

  stompClient.send("/pub/chat/message", {}, JSON.stringify(message));
};
