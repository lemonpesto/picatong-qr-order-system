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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public void update(String name, Integer price, Category category, Boolean isActive, String description) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.isActive = isActive;
        this.description = description;
    }
}
