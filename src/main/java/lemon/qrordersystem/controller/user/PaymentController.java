package lemon.qrordersystem.controller.user;

import lemon.qrordersystem.dto.CartSummaryDto;
import lemon.qrordersystem.entity.order.Order;
import lemon.qrordersystem.security.CustomUserDetails;
import lemon.qrordersystem.service.CartService;
import lemon.qrordersystem.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {
    
    private final CartService cartService;
    private final OrderService orderService;

    /**
     * 결제 페이지
     */
    @GetMapping("/{orderId}")
    public String payment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId,
            Model model) {

        CartSummaryDto summary = cartService.getCartSummary(userDetails.getId());

        model.addAttribute("summary", summary);
        model.addAttribute("orderId", orderId);

        model.addAttribute("navTitle", "결제");
        model.addAttribute("tableNum", userDetails.getTableNum());
        model.addAttribute("tableId", userDetails.getId());

        return "user/payment";
    }

    /**
     * 고객이 주문 취소
     * - 주문 삭제
     * - 장바구니 상태 ACTIVE로 변경
     */
    @PostMapping("/{orderId}/cancel")
    public String cancelPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {

        Long tableId = userDetails.getId();

        // 주문 삭제
        orderService.deleteOrder(orderId, tableId);

        // 장바구니 상태: ORDERING -> ACTIVE
        cartService.unlockCartToActive(tableId);

        return "redirect:/cart";
    }

    /**
     * 입금 완료 확인 요청
     */
    @PostMapping("/{orderId}/confirm")
    public String requestPaymentConfirm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {

        Long tableId = userDetails.getId();

        // 입금 완료 요청 및 장바구니 비우기
        orderService.requestPaymentConfirm(orderId, tableId);

        return "redirect:/payments/confirm";
    }

    /**
     * 결제 확인 페이지
     */
    @GetMapping("/confirm")
    public String confirmPage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        model.addAttribute("navTitle", "결제확인");
        model.addAttribute("tableNum", userDetails.getTableNum());
        model.addAttribute("tableId", userDetails.getId());
        return "user/confirm";
    }

    /**
     * 주문 접수 페이지
     */
    @GetMapping("/confirm/success")
    public String confirmSuccess(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId,
            Model model) {

        model.addAttribute("navTitle", "결제확인");
        model.addAttribute("tableNum", userDetails.getTableNum());
        return "user/confirm-success";
    }

    /**
     * 관리자 주도 주문 취소 페이지
     */
    @GetMapping("/confirm/cancel")
    public String confirmCancel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        model.addAttribute("navTitle", "결제취소");
        model.addAttribute("tableNum", userDetails.getTableNum());
        return "user/confirm-cancel";
    }
}
