package lemon.qrordersystem.entity.order;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lemon.qrordersystem.entity.table.TableEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "table_id", nullable = false)
    private TableEntity table;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PAYMENT_PENDING;

    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;

    private Integer totalAmount;
    private Integer totalQuantity;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    /* 연관관계 편의 */

    public void addItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    public void setTotals(int totalAmount, int totalQuantity) {
        this.totalAmount = totalAmount;
        this.totalQuantity = totalQuantity;
    }

    /* 상태 변경 */

    /** 고객: 입금완료 버튼 */
    public void requestPaymentConfirm() {
        if (this.status != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("입금완료 처리 가능한 상태가 아닙니다.");
        }
        this.status = OrderStatus.PAYMENT_CONFIRM_WAITING;
    }

    /** 관리자: 입금 확인 */
    public void confirmPayment() {
        if (this.status != OrderStatus.PAYMENT_CONFIRM_WAITING) {
            throw new IllegalStateException("입금 확인 가능한 상태가 아닙니다.");
        }
        this.status = OrderStatus.COOKING;
        this.confirmedAt = LocalDateTime.now();
    }

    /** 주방: 조리 완료 */
    public void completeCooking() {
        if (this.status == OrderStatus.COMPLETED) return;
        if (this.status != OrderStatus.COOKING) {
            throw new IllegalStateException("조리 완료 가능한 상태가 아닙니다.");
        }
        this.status = OrderStatus.SERVING;
    }

    /** 서버: 서빙 완료 */
    public void completeServing() {
        if (this.status != OrderStatus.SERVING && this.status != OrderStatus.COOKING) {
            throw new IllegalStateException("서빙 완료 가능한 상태가 아닙니다.");
        }
        this.status = OrderStatus.COMPLETED;
    }

    /** 관리자 주도 주문 취소 */
    public void cancel() {
        if (this.status == OrderStatus.COMPLETED || this.status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("취소할 수 없는 상태입니다.");
        }
        this.status = OrderStatus.CANCELLED;
    }
}
