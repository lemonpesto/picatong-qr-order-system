package lemon.qrordersystem.entity.cart;

import jakarta.persistence.*;
import lemon.qrordersystem.entity.item.Item;
import lombok.*;

@Entity
@Table(name = "cart_item")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    public void changeQuantity(int newQuantity) {
        if (newQuantity < 1) {
            throw new IllegalArgumentException("quantity must be >= 1");
        }
        this.quantity = newQuantity;
    }
}
