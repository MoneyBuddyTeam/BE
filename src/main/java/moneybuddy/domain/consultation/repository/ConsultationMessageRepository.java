package moneybuddy.domain.consultation.repository;

import moneybuddy.domain.consultation.entity.ConsultationMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultationMessageRepository extends JpaRepository<ConsultationMessage, Long> {

    /**
     * 삭제되지 않은 메시지 리스트 조회 (보낸 사람 기준)
     *
     * @param consultationRoomId 상담방 ID
     * @return 삭제되지 않은 메시지 리스트
     */
    List<ConsultationMessage> findByConsultationRoomIdAndIsDeletedBySenderFalse(Long consultationRoomId);

    /**
     * 전체 메시지 조회 (삭제 여부 관계 없음)
     *
     * @param roomId 상담방 ID
     * @return 전체 메시지 리스트
     */
    List<ConsultationMessage> findByConsultationRoomId(Long roomId);

    /**
     * 삭제되지 않은 메시지 페이징 조회 (보낸 사람 기준)
     *
     * @param roomId 상담방 ID
     * @param pageable 페이징 정보
     * @return 페이지 단위 메시지 목록
     */
    Page<ConsultationMessage> findByConsultationRoomIdAndIsDeletedBySenderFalse(Long roomId, Pageable pageable);

    long countByConsultationRoomIdAndIdGreaterThan(Long consultationRoomId, Long messageId);
}
