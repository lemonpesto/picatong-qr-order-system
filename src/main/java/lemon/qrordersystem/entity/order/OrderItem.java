package lemon.qrordersystem.entity.order;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lemon.qrordersystem.entity.item.Item;
import lombok.*;

@Entity
@Table(name = "orders_item")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private int orderPrice; // 주문 당시 가격

    @Column(nullable = false)
    private int quantity;   // 주문 수량

    @Builder.Default
    @Column(nullable = false)
    private Boolean cooked = false; // 조리 완료 여부

    @Builder.Default
    @Column(nullable = false)
    private Boolean served = false; // 서빙 완료 여부

    public void setOrder(Order order) {
        this.order = order;
        order.getOrderItems().add(this);
    }

    public void markAsCooked() {
        this.cooked = true;
    }

    public void markAsUncooked() {
        this.cooked = false;
    }

    public void markAsServed() {
        this.served = true;
    }

    public void markAsUnserved() {
        this.served = false;
    }
}
