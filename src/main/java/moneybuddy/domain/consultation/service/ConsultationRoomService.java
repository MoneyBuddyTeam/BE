package moneybuddy.domain.consultation.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import moneybuddy.domain.consultation.dto.ConsultationRoomCreateRequestDto;
import moneybuddy.domain.consultation.dto.ConsultationRoomDetailDto;
import moneybuddy.domain.consultation.dto.ConsultationRoomSummaryDto;
import moneybuddy.domain.consultation.entity.ConsultationOrder;
import moneybuddy.domain.consultation.entity.ConsultationRoom;
import moneybuddy.domain.consultation.repository.ConsultationMessageRepository;
import moneybuddy.domain.consultation.repository.ConsultationOrderRepository;
import moneybuddy.domain.consultation.repository.ConsultationRoomRepository;
import moneybuddy.domain.user.entity.User;
import moneybuddy.domain.user.repository.UserRepository;
import moneybuddy.global.enums.ConsultationStatus;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상담방 생성, 조회, 읽음 처리 등의 비즈니스 로직을 담당하는 서비스 클래스입니다.
 * <p>
 * - 상담방은 상담 주문(ConsultationOrder) 1건과 1:1로 연결됩니다.
 * - 상담사와 내담자 중 하나가 로그인한 상태에서 접근 가능합니다.
 * - 각 채팅방에 대해 상대방 정보, 마지막 메시지, 안 읽은 메시지 수 등을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class ConsultationRoomService {

    private final ConsultationRoomRepository roomRepository;
    private final ConsultationMessageRepository messageRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ConsultationOrderRepository consultationOrderRepository;
    private final ConsultationRoomRepository consultationRoomRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createOrGetRoom(Long clientId, ConsultationRoomCreateRequestDto dto) {
        String redisKey = "chat:room:lookup:" + clientId + ":" + dto.consultantId();

        String existingRoomId = redisTemplate.opsForValue().get(redisKey);
        if (existingRoomId != null) {
            return Long.parseLong(existingRoomId);
        }

        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("내담자 정보를 찾을 수 없습니다."));
        User consultant = userRepository.findById(dto.consultantId())
                .orElseThrow(() -> new IllegalArgumentException("상담사 정보를 찾을 수 없습니다."));

        ConsultationOrder order = ConsultationOrder.builder()
                .client(client)
                .consultant(consultant)
                .topic(dto.topic())
                .durationMinutes(dto.durationMinutes())
                .amount(dto.amount())
                .paymentMethod(dto.paymentMethod())
                .paidAt(LocalDateTime.now())
                .status(ConsultationStatus.RESERVED)
                .build();

        consultationOrderRepository.save(order);

        ConsultationRoom room = ConsultationRoom.builder()
                .consultationOrder(order)
                .isClosed(false).build();

        consultationRoomRepository.save(room);

        redisTemplate.opsForValue().set(redisKey, String.valueOf(room.getId()));
        redisTemplate.opsForSet().add(
                "chat:participants:" + room.getId(),
                String.valueOf(clientId),
                String.valueOf(dto.consultantId())
        );

        return room.getId();
    }


    @Transactional(readOnly = true)
    public List<ConsultationRoomSummaryDto> getConsultationRoomForUser(User loginUser) {
        return roomRepository.findByConsultationOrder_ClientOrConsultationOrder_ConsultantOrderByLastMessageAtDesc(loginUser, loginUser)
                .stream()
                .map(room -> {
                    User opponent = resolveOpponent(room, loginUser);
                    ConsultationOrder order = room.getConsultationOrder();

                    // ✅ Redis에서 lastReadId 조회
                    String redisKey = String.format("chat:lastRead:%d:%d", room.getId(), loginUser.getId());
                    String lastReadIdStr = redisTemplate.opsForValue().get(redisKey);
                    long lastReadId = -1L;

                    try {
                        if (lastReadIdStr != null) {
                            lastReadId = Long.parseLong(lastReadIdStr);
                        }
                    } catch (NumberFormatException ignored) {
                    }

                    // ✅ lastReadId보다 큰 메시지 개수 조회
                    long unreadCount = messageRepository.countByConsultationRoomIdAndIdGreaterThan(room.getId(), lastReadId);

                    return new ConsultationRoomSummaryDto(
                            room.getId(),
                            order.getTopic(),
                            opponent.getId(),
                            opponent.getNickname(),
                            opponent.getProfileImage(),
                            room.getLastMessage(),
                            room.getLastMessageAt(),
                            room.isClosed(),
                            (int) unreadCount
                    );
                })
                .toList();
    }


    /**
     * 특정 상담방의 상세 정보를 조회합니다. (접근 권한 포함)
     *
     * @param roomId    상담방 ID
     * @param loginUser 현재 로그인한 사용자
     * @return 상담방 상세 정보 DTO
     * @throws IllegalArgumentException 권한 없거나 상담방이 없을 경우
     */
    @Transactional(readOnly = true)
    public ConsultationRoomDetailDto getConsultationRoomDetail(Long roomId, User loginUser) {
        ConsultationRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("상담방을 찾을 수 없습니다."));

        ConsultationOrder order = room.getConsultationOrder();
        if (!loginUser.equals(order.getClient()) && !loginUser.equals(order.getConsultant())) {
            throw new IllegalArgumentException("해당 상담방에 접근할 수 없습니다.");
        }

        User opponent = resolveOpponent(room, loginUser);

        return new ConsultationRoomDetailDto(
                room.getId(),
                order.getTopic(),
                opponent.getNickname(),
                opponent.getProfileImage()
        );
    }

    /**
     * 특정 상담방에서 사용자가 읽지 않은 메시지를 모두 읽음 처리합니다.
     *
     * @param roomId 상담방 ID
     * @param userId 읽은 사용자 ID
     */
    @Transactional(readOnly = true)
    public void markMessagesAsRead(Long roomId, Long userId) {
        // Redis key: 메시지 리스트와 읽음 위치
        String messageListKey = "chat:messages:" + roomId;
        String lastReadKey = String.format("chat:lastRead:%d:%d", roomId, userId);
        String unreadKey = String.format("chat:unread:%d:%d", roomId, userId);

        // 가장 최근 메시지 ID 가져오기 (리스트의 마지막 요소)
        String latestMessageId = redisTemplate.opsForList().index(messageListKey, -1);

        if (latestMessageId != null) {
            // 읽음 위치 업데이트
            redisTemplate.opsForValue().set(lastReadKey, latestMessageId);
            // 안 읽은 메시지 수 초기화
            redisTemplate.delete(unreadKey);
        }
    }


    /**
     * 현재 로그인한 사용자의 상대방을 반환합니다.
     *
     * @param room 상담방
     * @param user 현재 로그인한 사용자
     * @return 상대방 User 객체
     */
    private User resolveOpponent(ConsultationRoom room, User user) {
        ConsultationOrder order = room.getConsultationOrder();
        return user.equals(order.getClient()) ? order.getConsultant() : order.getClient();
    }


    @Transactional
    public void updateConsultationStatus(Long consultationId, Long userId, ConsultationStatus newStatus) {
        ConsultationOrder consultation = consultationOrderRepository.findById(consultationId)
                .orElseThrow(() -> new EntityNotFoundException("해당 상담이 존재하지 않습니다."));

        Long clientId = consultation.getClient().getId();
        Long advisorId = consultation.getConsultant().getId();
        ConsultationStatus currentStatus = consultation.getStatus();

        // 역할 및 상태 흐름 검증
        if (userId.equals(clientId)) {
            // 내담자: 상태 변경 허용 안 함 or 요청 취소 처리
            throw new IllegalArgumentException("내담자는 상담 상태를 변경할 수 없습니다.");
        } else if (userId.equals(advisorId)) {
            // 전문가 상태 흐름 검증
            if (currentStatus == ConsultationStatus.RESERVED && newStatus == ConsultationStatus.SCHEDULED) {
                consultation.setStatus(ConsultationStatus.SCHEDULED);
            } else if (currentStatus == ConsultationStatus.SCHEDULED && newStatus == ConsultationStatus.COMPLETED) {
                consultation.setStatus(ConsultationStatus.COMPLETED);
            } else {
                throw new IllegalStateException("상담 상태를 변경할 수 없습니다. 현재 상태: " + currentStatus + ", 요청 상태: " + newStatus);
            }
        } else {
            throw new SecurityException("상담 상태를 변경할 권한이 없습니다.");
        }
    }
}
