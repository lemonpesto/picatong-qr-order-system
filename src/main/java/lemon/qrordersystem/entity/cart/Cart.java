package lemon.qrordersystem.entity.cart;

import jakarta.persistence.*;
import lemon.qrordersystem.entity.table.TableEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private TableEntity table;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CartStatus status = CartStatus.ACTIVE;

    private LocalDateTime lockedAt;

    @Version
    private Long version;

    public boolean isEditable() {
        return this.status == CartStatus.ACTIVE;
    }

    public void lockForOrdering() {
        if (this.status != CartStatus.ACTIVE) {
            throw new IllegalStateException("Cart is not ACTIVE");
        }
        this.status = CartStatus.ORDERING;
        this.lockedAt = LocalDateTime.now();
    }

    public void unlockToActive() {
        if (this.status != CartStatus.ORDERING) {
            throw new IllegalStateException("Cart is not ORDERING");
        }
        this.status = CartStatus.ACTIVE;
        this.lockedAt = null;
    }
}
