// src/services/chatUtils.ts
import { IMessage } from "@stomp/stompjs";
import { ChatMessage } from "../types";

/**
 * 서버로부터 받은 메시지 프레임을 ChatMessage 객체로 변환
 */
export function parseMessageFrame(frame: IMessage): ChatMessage {
  return JSON.parse(frame.body);
}
