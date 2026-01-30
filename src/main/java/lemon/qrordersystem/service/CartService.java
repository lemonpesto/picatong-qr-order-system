package lemon.qrordersystem.service;

import lemon.qrordersystem.dto.CartSummaryDto;
import lemon.qrordersystem.entity.cart.Cart;
import lemon.qrordersystem.entity.cart.CartItem;
import lemon.qrordersystem.entity.cart.CartStatus;
import lemon.qrordersystem.entity.item.Item;
import lemon.qrordersystem.entity.table.TableEntity;
import lemon.qrordersystem.repository.CartItemRepository;
import lemon.qrordersystem.repository.CartRepository;
import lemon.qrordersystem.repository.ItemRepository;
import lemon.qrordersystem.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final TableRepository tableRepository;
    private final ItemRepository itemRepository;

    /**
     * 테이블의 활성 장바구니 조회 (없으면 생성)
     */
    @Transactional
    public Cart getOrCreateCart(Long tableId) {
        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("테이블을 찾을 수 없습니다."));

        return cartRepository.findByTable_IdAndStatus(tableId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .table(table)
                            .status(CartStatus.ACTIVE)
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    /**
     * 장바구니에 아이템 추가
     */
    @Transactional
    public void addToCart(Long tableId, Long itemId, Integer quantity) {
        Cart cart = getOrCreateCart(tableId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("메뉴를 찾을 수 없습니다."));

        if (!item.getIsActive()) {
            throw new RuntimeException("품절된 상품입니다.");
        }

        // 기존 장바구니 아이템 확인
        Optional<CartItem> existingCartItem = cartItemRepository
                .findByCartIdAndItemId(cart.getId(), itemId);

        if (existingCartItem.isPresent()) {
            // 수량 추가
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItemRepository.save(cartItem);
        } else {
            // 새로운 아이템 추가
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .item(item)
                    .quantity(quantity)
                    .build();
            cartItemRepository.save(cartItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    /**
     * 장바구니 아이템 수량 변경
     */
    @Transactional
    public void updateCartItemQuantity(Long tableId, Long itemId, Integer count) {
        Cart cart = getOrCreateCart(tableId);

        CartItem cartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), itemId)
                .orElseThrow(() -> new RuntimeException("장바구니에 해당 아이템이 없습니다."));

        if (count <= 0) {
            throw new RuntimeException("수량은 1개 이상이어야 합니다.");
        }

        cartItem.setQuantity(count);
        cartItemRepository.save(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    /**
     * 장바구니 아이템 삭제
     */
    @Transactional
    public void removeCartItem(Long tableId, Long itemId) {
        Cart cart = getOrCreateCart(tableId);

        CartItem cartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), itemId)
                .orElseThrow(() -> new RuntimeException("장바구니에 해당 아이템이 없습니다."));

        cartItemRepository.delete(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    /**
     * 장바구니 요약 정보 조회
     */
    public CartSummaryDto getCartSummary(Long tableId) {
        Optional<Cart> cartOpt = cartRepository
                .findByTable_IdAndStatus(tableId, CartStatus.ACTIVE);

        if (cartOpt.isEmpty()) {
            return new CartSummaryDto(0, 0);
        }

        Cart cart = cartOpt.get();
        List<CartItem> items = cart.getCartItems();

        int totalQuantity = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        int totalAmount = items.stream()
                .mapToInt(ci -> ci.getItem().getPrice() * ci.getQuantity())
                .sum();

        return new CartSummaryDto(totalQuantity, totalAmount);
    }

    /**
     * 장바구니 아이템 목록 조회
     */
    public List<CartItem> getCartItems(Long tableId) {
        Cart cart = getOrCreateCart(tableId);
        return cartItemRepository.findByCartId(cart.getId());
    }

    /**
     * 장바구니 비우기
     */
    @Transactional
    public void clearCart(Long tableId) {
        Optional<Cart> cartOpt = cartRepository
                .findByTable_IdAndStatus(tableId, CartStatus.ACTIVE);

        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cartItemRepository.deleteByCartId(cart.getId());
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
        }
    }
}
