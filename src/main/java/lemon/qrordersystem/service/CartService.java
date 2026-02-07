package lemon.qrordersystem.service;

import lemon.qrordersystem.dto.CartSummaryDto;
import lemon.qrordersystem.dto.CartSyncDto;
import lemon.qrordersystem.dto.CartUpdatedMessage;
import lemon.qrordersystem.entity.cart.Cart;
import lemon.qrordersystem.entity.cart.CartItem;
import lemon.qrordersystem.entity.cart.CartStatus;
import lemon.qrordersystem.entity.item.Item;
import lemon.qrordersystem.entity.table.TableEntity;
import lemon.qrordersystem.exception.BusinessException;
import lemon.qrordersystem.exception.ItemNotFoundException;
import lemon.qrordersystem.exception.ItemSoldOutException;
import lemon.qrordersystem.repository.CartItemRepository;
import lemon.qrordersystem.repository.CartRepository;
import lemon.qrordersystem.repository.ItemRepository;
import lemon.qrordersystem.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final TableRepository tableRepository;
    private final ItemRepository itemRepository;

    /**
     * 사용 가능한 장바구니 조회, 없으면 생성
     */
    public Cart getOrCreateCart(Long tableId) {
        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> new BusinessException("테이블을 찾을 수 없습니다."));

        // 1) 주문 중인 장바구니 우선 조회 (중복이 있으면 정리)
        Optional<Cart> ordering = findAndHealOrderingCart(tableId);
        if (ordering.isPresent()) {
            return ordering.get();
        }

        // 2) ACTIVE 있으면 그대로 사용
        return cartRepository.findByTable_IdAndStatus(tableId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart cart = Cart.builder()
                            .table(table)
                            .status(CartStatus.ACTIVE)
                            .build();
                    return cartRepository.save(cart);
                });
    }

    /**
     * 장바구니 상태를 ORDERING -> ACTIVE로 되돌림
     * - ORDERING이 여러 개면(데이터 꼬임) 최신 1개만 살리고 나머지는 삭제/정리함
     */
    public void unlockCartToActive(Long tableId) {
        Optional<Cart> ordering = findAndHealOrderingCart(tableId);
        ordering.ifPresent(Cart::unlockToActive);
    }

    private Optional<Cart> findAndHealOrderingCart(Long tableId) {
        List<Cart> orderingCarts =
                cartRepository.findAllByTable_IdAndStatusOrderByIdDesc(tableId, CartStatus.ORDERING);

        if (orderingCarts.isEmpty()) return Optional.empty();

        Cart keep = orderingCarts.get(0);

        if (orderingCarts.size() > 1) {
            log.warn("[DUP_CART] tableId={} ORDERING carts={} -> keep cartId={}",
                    tableId, orderingCarts.size(), keep.getId());

            for (int i = 1; i < orderingCarts.size(); i++) {
                Cart dup = orderingCarts.get(i);
                // cartItem 정리 후 cart 삭제
                cartItemRepository.deleteByCartId(dup.getId());
                cartRepository.delete(dup);
            }
        }

        return Optional.of(keep);
    }

    /**
     * 장바구니에 아이템 추가
     */
    public void addItem(Long tableId, Long itemId, Integer quantity) {
        Cart cart = getOrCreateCart(tableId);
        assertEditable(cart);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        if (!item.getIsActive()) {
            throw new ItemSoldOutException(itemId);
        }

        // 기존 장바구니 아이템 확인
        CartItem existingCartItem = cart.getCartItems().stream()
                .filter(ci -> ci.getItem().getId().equals(itemId))
                .findFirst()
                .orElse(null);

        // 기존에 담아놨던 아이템이라면
        if (existingCartItem != null) {
            // 수량 추가
            existingCartItem.changeQuantity(existingCartItem.getQuantity() + quantity);
        } else {
            // 새로운 아이템 추가
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .item(item)
                    .quantity(quantity)
                    .build();

            cart.getCartItems().add(cartItem);
        }
    }

    /**
     * 장바구니 아이템 수량 변경
     */
    public void updateQuantity(Long tableId, Long itemId, Integer quantity) {
        Cart cart = getOrCreateCart(tableId);
        assertEditable(cart);

        CartItem ci = cart.getCartItems().stream()
                .filter(x -> x.getItem() != null && x.getItem().getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("장바구니에 해당 아이템이 없습니다."));

        if (quantity <= 0) {
            cart.getCartItems().remove(ci);
        } else {
            ci.changeQuantity(quantity);
        }
    }

    /**
     * 장바구니 아이템 삭제
     */
    public void deleteItem(Long tableId, Long itemId) {
        Cart cart = getOrCreateCart(tableId);
        assertEditable(cart);

        boolean removed = cart.getCartItems().removeIf(ci ->
                ci.getItem() != null && ci.getItem().getId().equals(itemId)
        );

        if (!removed) {
            throw new BusinessException("장바구니에 해당 아이템이 없습니다.");
        }
    }

    /**
     * 장바구니 요약 정보 조회
     */
    @Transactional(readOnly = true)
    public CartSummaryDto getCartSummary(Long tableId) {
        Optional<Cart> cartOpt = cartRepository.findFirstByTable_IdAndStatusInOrderByIdDesc(
                tableId, List.of(CartStatus.ORDERING, CartStatus.ACTIVE)
        );

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
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long tableId) {
        Cart cart = getOrCreateCart(tableId);
        assertEditable(cart);

        return cart.getCartItems();
    }

    /**
     * 장바구니 비우기
     */
    public void clearCart(Long tableId) {
        Optional<Cart> cartOpt = cartRepository
                .findFirstByTable_IdAndStatusInOrderByIdDesc(
                        tableId, List.of(CartStatus.ACTIVE, CartStatus.ORDERING)
                );

        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cartItemRepository.deleteByCartId(cart.getId());
            cart.getCartItems().clear();
        }
    }

    private void assertEditable(Cart cart) {
        if (!cart.isEditable()) {
            throw new BusinessException("현재 주문 중입니다.");
        }
    }

    @Transactional(readOnly = true)
    public CartSyncDto buildCartSyncDto(Long tableId) {
        Optional<Cart> cartOpt = cartRepository.findFirstByTable_IdAndStatusInOrderByIdDesc(
                tableId, List.of(CartStatus.ORDERING, CartStatus.ACTIVE)
        );

        if (cartOpt.isEmpty()) {
            return new CartSyncDto(new CartSummaryDto(0, 0), List.of(), false);        }

        Cart cart = cartOpt.get();

        List<CartSyncDto.CartLineDto> lines = cart.getCartItems().stream()
                .filter(ci -> ci.getItem() != null)
                .map(ci -> new CartSyncDto.CartLineDto(
                        ci.getItem().getId(),
                        ci.getItem().getName(),
                        ci.getItem().getPrice(),
                        ci.getQuantity(),
                        ci.getItem().getPrice() * ci.getQuantity()
                ))
                .toList();

        CartSummaryDto summary = new CartSummaryDto(
                lines.stream().mapToInt(CartSyncDto.CartLineDto::quantity).sum(),
                lines.stream().mapToInt(CartSyncDto.CartLineDto::lineAmount).sum()
        );

        boolean ordering = cart.getStatus() == CartStatus.ORDERING;
        return new CartSyncDto(summary, lines, ordering);    }
}