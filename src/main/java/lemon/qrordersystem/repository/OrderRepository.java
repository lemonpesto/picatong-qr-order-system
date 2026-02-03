package lemon.qrordersystem.repository;

import lemon.qrordersystem.entity.order.Order;
import lemon.qrordersystem.entity.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatusInOrderByCreatedAtAsc(List<OrderStatus> statuses);
    List<Order> findByStatusOrderByConfirmedAtAsc(OrderStatus status);
    List<Order> findByStatusOrderByCreatedAtAsc(OrderStatus status);

    List<Order> findByTable_TableNumOrderByCreatedAtDesc(Integer tableNum);
    List<Order> findAllByOrderByCreatedAtDesc();

    Optional<Order> findFirstByTable_IdAndStatusOrderByCreatedAtDesc(Long tableId, OrderStatus status);

    List<Order> findByTable_IdAndStatusAndCreatedAtBefore(
            Long tableId,
            OrderStatus status,
            LocalDateTime createdAt
    );
}
