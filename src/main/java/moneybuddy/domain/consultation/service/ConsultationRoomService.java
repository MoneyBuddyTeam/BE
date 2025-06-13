package moneybuddy.domain.consultation.service;

import lombok.RequiredArgsConstructor;
import moneybuddy.domain.consultation.dto.ConsultationRoomDetailDto;
import moneybuddy.domain.consultation.dto.ConsultationRoomSummaryDto;
import moneybuddy.domain.consultation.entity.ConsultationMessage;
import moneybuddy.domain.consultation.entity.ConsultationOrder;
import moneybuddy.domain.consultation.entity.ConsultationRoom;
import moneybuddy.domain.consultation.repository.ConsultationMessageRepository;
import moneybuddy.domain.consultation.repository.ConsultationRoomRepository;
import moneybuddy.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 상담방 생성, 조회, 읽음 처리 등의 비즈니스 로직을 담당하는 서비스 클래스입니다.
 *
 * - 상담방은 상담 주문(ConsultationOrder) 1건과 1:1로 연결됩니다.
 * - 상담사와 내담자 중 하나가 로그인한 상태에서 접근 가능합니다.
 * - 각 채팅방에 대해 상대방 정보, 마지막 메시지, 안 읽은 메시지 수 등을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class ConsultationRoomService {

    private final ConsultationRoomRepository roomRepository;
    private final ConsultationMessageRepository messageRepository;

    /**
     * 상담 주문(ConsultationOrder)을 기반으로 상담방을 생성하거나 기존 상담방을 반환합니다.
     *
     * @param order 결제 완료된 상담 주문
     * @return 생성되었거나 기존의 상담방
     */
    @Transactional
    public ConsultationRoom createConsultationRoom(ConsultationOrder order) {
        return roomRepository.findByConsultationOrder(order)
                .orElseGet(() -> {
                    ConsultationRoom room = ConsultationRoom.builder()
                            .consultationOrder(order)
                            .isClosed(false)
                            .build();
                    return roomRepository.save(room);
                });
    }

    /**
     * 현재 로그인한 사용자가 참여 중인 상담방 목록(요약 정보 포함)을 반환합니다.
     *
     * @param loginUser 현재 로그인한 사용자
     * @return 상담방 요약 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ConsultationRoomSummaryDto> getConsultationRoomForUser(User loginUser) {
        return roomRepository.findByConsultationOrder_ClientOrConsultationOrder_ConsultantOrderByLastMessageAtDesc(loginUser, loginUser)
                .stream()
                .map(room -> {
                    User opponent = resolveOpponent(room, loginUser);
                    int unreadCount = messageRepository.countByConsultationRoomAndReceiverAndIsReadFalse(room, loginUser);
                    ConsultationOrder order = room.getConsultationOrder();

                    return new ConsultationRoomSummaryDto(
                            room.getId(),
                            order.getTopic(),
                            opponent.getId(),
                            opponent.getNickname(),
                            opponent.getProfileImage(),
                            room.getLastMessage(),
                            room.getLastMessageAt(),
                            room.isClosed(),
                            unreadCount
                    );
                }).toList();
    }

    /**
     * 특정 상담방의 상세 정보를 조회합니다. (접근 권한 포함)
     *
     * @param roomId 상담방 ID
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
    @Transactional
    public void markMessagesAsRead(Long roomId, Long userId) {
        List<ConsultationMessage> unreadMessages = messageRepository
                .findByConsultationRoomIdAndReceiverIdAndIsReadFalse(roomId, userId);

        for (ConsultationMessage msg : unreadMessages) {
            msg.setRead(true);
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
}
