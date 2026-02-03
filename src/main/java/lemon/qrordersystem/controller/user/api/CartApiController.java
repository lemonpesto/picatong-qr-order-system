package lemon.qrordersystem.controller.user.api;

import lemon.qrordersystem.dto.AddToCartRequest;
import lemon.qrordersystem.dto.CartSummaryDto;
import lemon.qrordersystem.security.CustomUserDetails;
import lemon.qrordersystem.service.CartService;
import lemon.qrordersystem.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;
    private final WebSocketService webSocketService;

    /**
     * 장바구니에 메뉴 추가
     */
    @PostMapping("/cart/add")
    public ResponseEntity<CartSummaryDto> addToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AddToCartRequest req) {

        Long tableId = userDetails.getId();
        cartService.addItem(tableId, req.getItemId(), req.getQuantity());

        // 트랜잭션 커밋 후 WebSocket 알림
        CartSummaryDto summary = cartService.getCartSummary(tableId);
        webSocketService.tableCartUpdated(tableId, cartService.buildCartSyncDto(tableId));

        return ResponseEntity.ok(summary);
    }

    /**
     * 장바구니 요약 정보 조회
     */
    @GetMapping("/cart/summary")
    public ResponseEntity<CartSummaryDto> getCartSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long tableId = userDetails.getId();
        return ResponseEntity.ok(cartService.getCartSummary(tableId));
    }

    /**
     * 장바구니 아이템 수량 변경
     */
    @PostMapping("/cart/update")
    public ResponseEntity<CartSummaryDto> updateCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long itemId,
            @RequestBody Map<String, Integer> body) {

        Long tableId = userDetails.getId();
        cartService.updateQuantity(tableId, itemId, body.get("qty"));

        // 트랜잭션 커밋 후 WebSocket 알림
        CartSummaryDto summary = cartService.getCartSummary(tableId);
        webSocketService.tableCartUpdated(tableId, cartService.buildCartSyncDto(tableId));

        return ResponseEntity.ok(summary);
    }

    /**
     * 장바구니 아이템 삭제
     */
    @DeleteMapping("/cart/delete")
    public ResponseEntity<CartSummaryDto> deleteCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long itemId) {

        Long tableId = userDetails.getId();
        cartService.deleteItem(tableId, itemId);

        // 트랜잭션 커밋 후 WebSocket 알림
        CartSummaryDto summary = cartService.getCartSummary(tableId);
        webSocketService.tableCartUpdated(tableId, cartService.buildCartSyncDto(tableId));

        return ResponseEntity.ok(summary);
    }
}