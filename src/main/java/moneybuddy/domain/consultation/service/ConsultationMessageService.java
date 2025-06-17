package moneybuddy.domain.consultation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moneybuddy.domain.consultation.dto.ConsultationMessageDto;
import moneybuddy.domain.consultation.dto.ConsultationMessageResponseDto;
import moneybuddy.domain.consultation.entity.ConsultationMessage;
import moneybuddy.domain.consultation.entity.ConsultationRoom;
import moneybuddy.domain.consultation.repository.ConsultationMessageRepository;
import moneybuddy.domain.consultation.repository.ConsultationRoomRepository;
import moneybuddy.domain.user.entity.User;
import moneybuddy.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsultationMessageService {

    private final ConsultationMessageRepository messageRepository;
    private final ConsultationRoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ConsultationMessageRepository consultationMessageRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public ConsultationMessageResponseDto saveMessage(ConsultationMessageDto dto) {
        ConsultationRoom room = roomRepository.findById(dto.consultationRoomId())
                .orElseThrow(() -> new IllegalArgumentException("상담방을 찾을 수 없습니다."));

        User sender = userRepository.findById(dto.senderId())
                .orElseThrow(() -> new IllegalArgumentException("보낸 사용자를 찾을 수 없습니다."));

        User receiver = (room.getConsultationOrder().getClient().equals(sender))
                ? room.getConsultationOrder().getConsultant()
                : room.getConsultationOrder().getClient();

        ConsultationMessage message = ConsultationMessage.builder()
                .consultationRoom(room)
                .sender(sender)
                .receiver(receiver)
                .message(dto.message())
                .type(dto.type())
                .imageUrl(dto.imageUrl())
                .sentAt(dto.sentAt() != null ? dto.sentAt() : LocalDateTime.now())
                .isDeletedBySender(false).isDeletedByReceiver(false).build();

        // 1️⃣ DB에 메시지 저장
        ConsultationMessage savedMessage = messageRepository.save(message);

        // 2️⃣ Redis에서 receiver의 lastRead 메시지 ID 조회
        String redisKey = String.format("chat:lastRead:%d:%d", room.getId(), receiver.getId());
        String lastReadIdStr = redisTemplate.opsForValue().get(redisKey);

        boolean isReadByReceiver = false;
        if (lastReadIdStr != null) {
            try {
                long lastReadId = Long.parseLong(lastReadIdStr);
                isReadByReceiver = lastReadId >= savedMessage.getId();
            } catch (NumberFormatException ignored) {
            }
        }

        // 3️⃣ 마지막 메시지 내용 갱신
        room.updateLastMessage(dto.message() != null ? dto.message() : "[이미지]");

        // 4️⃣ 메시지 응답 DTO 반환
        return ConsultationMessageResponseDto.from(savedMessage, isReadByReceiver);
    }


    @Transactional(readOnly = true)
    public List<ConsultationMessageResponseDto> getMessagesForConsultationRoom(Long roomId, User loginUser) {
        ConsultationRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("상담방을 찾을 수 없습니다."));

        User client = room.getConsultationOrder().getClient();
        User consultant = room.getConsultationOrder().getConsultant();

        if (!loginUser.equals(client) && !loginUser.equals(consultant)) {
            throw new IllegalArgumentException("해당 상담방에 접근할 수 없습니다.");
        }

        List<ConsultationMessage> messages = messageRepository.findByConsultationRoomId(roomId);

        // ✅ Redis에서 현재 로그인 사용자의 lastRead ID 가져오기
        String redisKey = String.format("chat:lastRead:%d:%d", roomId, loginUser.getId());
        String lastReadIdStr = redisTemplate.opsForValue().get(redisKey);
        long lastReadId = -1L;

        try {
            if (lastReadIdStr != null) {
                lastReadId = Long.parseLong(lastReadIdStr);
            }
        } catch (NumberFormatException ignored) {
        }

        // ✅ effectively final 변수로 복사
        final long finalLastReadId = lastReadId;

        return messages.stream()
                .filter(msg -> {
                    if (loginUser.equals(msg.getSender())) {
                        return !msg.isDeletedBySender();
                    } else {
                        return !msg.isDeletedByReceiver();
                    }
                })
                .map(msg -> {
                    boolean isReadByMe = loginUser.equals(msg.getReceiver()) && msg.getId() <= finalLastReadId;
                    return ConsultationMessageResponseDto.from(msg, isReadByMe);
                })
                .toList();
    }


    @Transactional
    public void leaveConsultationRoom(Long roomId, User loginUser) {
        ConsultationRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("상담방을 찾을 수 없습니다."));

        List<ConsultationMessage> messages = messageRepository.findByConsultationRoomId(roomId);

        for (ConsultationMessage msg : messages) {
            if (msg.getSender().equals(loginUser)) {
                msg.setDeletedBySender(true);
            } else {
                msg.setDeletedByReceiver(true);
            }
        }

        boolean allDeleted = messages.stream()
                .allMatch(m -> m.isDeletedBySender() && m.isDeletedByReceiver());

        if (allDeleted) {
            room.closeRoom();
        }
    }

    private ConsultationMessageDto toDto(ConsultationMessage message) {
        return new ConsultationMessageDto(
                message.getConsultationRoom().getId(),
                message.getSender().getId(),
                message.getSender().getNickname(),
                message.getMessage(),
                message.getType(),
                message.getImageUrl(),
                message.getSentAt()
        );
    }


    public Page<ConsultationMessageDto> getMessages(Long roomId, Pageable pageable) {
        return consultationMessageRepository.findByConsultationRoomIdAndIsDeletedBySenderFalse(roomId, pageable)
                .map(ConsultationMessageDto::fromEntity);
    }
}
