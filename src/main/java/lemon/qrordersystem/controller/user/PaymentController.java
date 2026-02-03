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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {
    
    private final CartService cartService;
    private final OrderService orderService;

    /**
     * 결제 페이지
     */
    @GetMapping
    public String payment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long orderId,
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
     * 고객 주도 주문 취소
     * - cart: ORDERING -> ACTIVE로 되돌림
     * - order: 삭제 (PAYMENT_PENDING 상태만 취소 가능)
     */
    @PostMapping("/cancel")
    public String cancelPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long orderId) {

        Long tableId = userDetails.getId();

        // 주문 취소 (PAYMENT_PENDING 상태 확인 포함)
        orderService.deleteOrder(orderId, tableId);

        // 장바구니 상태 복원: ORDERING -> ACTIVE
        cartService.unlockCartToActive(tableId);

        return "redirect:/cart";
    }

    /**
     * 입금 완료 확인 요청
     */
    @PostMapping
    public String requestPaymentConfirm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long orderId) {

        Long tableId = userDetails.getId();

        // 입금 완료 요청 및 장바구니 비우기
        orderService.requestPaymentConfirm(orderId, tableId);

        return "redirect:/payment/confirm?orderId=" + orderId;
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
