package lemon.qrordersystem.repository;

import lemon.qrordersystem.entity.cart.Cart;
import lemon.qrordersystem.entity.cart.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByTable_IdAndStatus(Long tableId, CartStatus status);
    Optional<Cart> findFirstByTable_IdAndStatusInOrderByIdDesc(Long tableId, List<CartStatus> statuses);
}
