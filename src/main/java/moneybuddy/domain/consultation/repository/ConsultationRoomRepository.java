package moneybuddy.domain.consultation.repository;

import moneybuddy.domain.consultation.entity.ConsultationOrder;
import moneybuddy.domain.consultation.entity.ConsultationRoom;
import moneybuddy.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConsultationRoomRepository extends JpaRepository<ConsultationRoom, Long> {

    /**
     * 특정 상담 주문에 해당하는 상담방 조회
     */
    Optional<ConsultationRoom> findByConsultationOrder(ConsultationOrder order);

    /**
     * 내담자 또는 상담사가 참여한 상담방 목록을 최근 메시지 순으로 조회
     */
    List<ConsultationRoom> findByConsultationOrder_ClientOrConsultationOrder_ConsultantOrderByLastMessageAtDesc(User client, User consultant);
}
