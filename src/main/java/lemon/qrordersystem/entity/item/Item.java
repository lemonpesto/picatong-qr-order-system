package lemon.qrordersystem.entity.item;

import jakarta.persistence.*;
import lemon.qrordersystem.entity.order.Order;
import lombok.*;

@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private String name;
    private Integer price;
    private String description;
    private Boolean isActive;

    public void update(String name, Integer price, Category category, Boolean isActive, String description) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.isActive = isActive;
        this.description = description;
    }
}
