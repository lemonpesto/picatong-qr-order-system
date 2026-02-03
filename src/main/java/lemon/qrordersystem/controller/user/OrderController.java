package lemon.qrordersystem.controller.user;

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

import java.util.List;

/**
 * 주문 관련 컨트롤러
 * - 주문 시작
 * - 주문 내역 조회
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;

    /**
     * 주문 생성: 주문하기 버튼을 눌렀을 때:
     * 1) ACTIVE 장바구니를 ORDERING으로 잠금
     * 2) 장바구니 아이템으로 Order 생성
     * 3) 결제 페이지로 redirect
     */
    @GetMapping("/create")
    public String orderCreate(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long tableId = userDetails.getId();

        // PAYMENT_PENDING 있으면 그걸로 결제 계속하기
        Order order = orderService.getOrCreatePaymentPendingOrder(tableId); // cart는 ORDERING 상태로 전이 && (order 생성 or get)
        return "redirect:/payment?orderId=" + order.getId();
    }

    /**
     * 주문 내역 조회
     */
    @GetMapping("/history")
    public String orderHistory(
            @RequestParam(required = false) String returnUrl,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {


        List<Order> orders = orderService.getOrdersByTable(userDetails.getTableNum());

        String safeReturn;
        if ("/items".equals(returnUrl) || "/cart".equals(returnUrl)) {
            safeReturn = returnUrl;
        } else {
            safeReturn = "/items";
        }

        model.addAttribute("orders", orders);
        model.addAttribute("tableNum", userDetails.getTableNum());
        model.addAttribute("navTitle", "주문내역");
        model.addAttribute("navBack", safeReturn);

        return "user/orders-history";
    }
}
