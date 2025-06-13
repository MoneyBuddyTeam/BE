package moneybuddy.domain.consultation.repository;

import moneybuddy.domain.consultation.entity.ConsultationMessage;
import moneybuddy.domain.consultation.entity.ConsultationRoom;
import moneybuddy.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConsultationMessageRepository extends JpaRepository<ConsultationMessage, Long> {

    /**
     * 특정 상담방에서 내가 아닌 사용자가 보낸 안 읽은 메시지 수를 반환합니다.
     *
     * @param room  상담방
     * @param user  현재 로그인한 사용자 (메시지를 읽는 주체)
     * @return 안 읽은 메시지 수
     */
    @Query("""
        SELECT COUNT(m) FROM ConsultationMessage m
        WHERE m.consultationRoom = :room
          AND m.sender <> :user
          AND m.isRead = false
    """)
    int countUnreadMessages(@Param("room") ConsultationRoom room, @Param("user") User user);

    /**
     * 특정 상담방 ID에서 내가 아닌 사용자가 보낸 안 읽은 메시지 수를 반환합니다.
     *
     * @param consultationRoomId 상담방 ID
     * @param userId 현재 사용자 ID
     * @return 안 읽은 메시지 수
     */
    @Query("""
        SELECT COUNT(m) FROM ConsultationMessage m
        WHERE m.consultationRoom.id = :consultationRoomId
          AND m.sender.id <> :userId
          AND m.isRead = false
    """)
    long countUnreadMessages(@Param("consultationRoomId") Long consultationRoomId, @Param("userId") Long userId);

    /**
     * Query Method 버전 - 특정 상담방에서 현재 유저가 받지 않은 메시지 수 조회
     *
     * @param consultationRoom 상담방
     * @param currentUser 현재 사용자
     * @return 안 읽은 메시지 수
     */
    int countByConsultationRoomAndSenderNotAndIsReadFalse(ConsultationRoom consultationRoom, User currentUser);

    /**
     * 삭제되지 않은 메시지 리스트 조회 (상담방 기준)
     *
     * @param consultationRoomId 상담방 ID
     * @return 삭제되지 않은 메시지 리스트
     */
    List<ConsultationMessage> findByConsultationRoomIdAndIsDeletedBySenderFalse(Long consultationRoomId);

    /**
     * 상담방 내 특정 수신자 기준 안 읽은 메시지 조회
     *
     * @param consultationRoomId 상담방 ID
     * @param receiverId 수신자 ID
     * @return 안 읽은 메시지 리스트
     */
    List<ConsultationMessage> findByConsultationRoomIdAndReceiverIdAndIsReadFalse(Long consultationRoomId, Long receiverId);

    List<ConsultationMessage> findByConsultationRoomId(Long roomId);

    int countByConsultationRoomAndReceiverAndIsReadFalse(ConsultationRoom room, User loginUser);
}
