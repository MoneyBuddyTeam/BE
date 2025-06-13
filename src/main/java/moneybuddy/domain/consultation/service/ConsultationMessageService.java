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

    @Transactional
    public void saveMessage(ConsultationMessageDto dto) {
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
                .isRead(false)
                .isDeletedBySender(false)
                .isDeletedByReceiver(false)
                .sentAt(dto.sentAt() != null ? dto.sentAt() : LocalDateTime.now())
                .build();

        messageRepository.save(message);

        room.updateLastMessage(dto.message() != null ? dto.message() : "[이미지]");
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

        return messages.stream()
                .filter(msg -> {
                    if (loginUser.equals(msg.getSender())) {
                        return !msg.isDeletedBySender();
                    } else {
                        return !msg.isDeletedByReceiver();
                    }
                })
                .map(ConsultationMessageResponseDto::from)
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
}
