package lemon.qrordersystem.entity.order;

import jakarta.persistence.*;
import lemon.qrordersystem.entity.item.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
//    @JsonIgnore
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
//    @JsonIgnore
    private Item item;

    private int orderPrice; // 주문 당시 가격
    private int quantity; // 주문 수량

    public void setOrder(Order order) {
        this.order = order;
        order.getOrderItems().add(this);
    }
}
