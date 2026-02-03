package lemon.qrordersystem.controller.admin;

import lemon.qrordersystem.entity.order.Order;
import lemon.qrordersystem.repository.TableRepository;
import lemon.qrordersystem.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final TableRepository tableRepository;

    /**
     * 홀 페이지 - 입금 확인 탭
     */
    @GetMapping("/hall")
    public String confirmPage(Model model) {
        model.addAttribute("confirmOrders", orderService.getConfirmOrders());
        model.addAttribute("serveOrders", orderService.getServeOrders());
        model.addAttribute("activeTab", "confirm");
        model.addAttribute("pageTitle", "홀");
        return "admin/orders-hall";
    }

    /**
     * 홀 페이지 - 서빙 탭
     */
    @GetMapping("/hall/serve")
    public String servePage(Model model) {
        model.addAttribute("confirmOrders", orderService.getConfirmOrders());
        model.addAttribute("serveOrders", orderService.getServeOrders());
        model.addAttribute("activeTab", "serve");
        model.addAttribute("pageTitle", "홀");
        return "admin/orders-hall";
    }

    /* 입금 확인 처리 */

    /**
     * 입금 확인 처리
     */
    @PostMapping("/{orderId}/confirm")
    public String confirmPayment(@PathVariable Long orderId,
                                 RedirectAttributes redirectAttrs) {
        orderService.confirmPayment(orderId);
        redirectAttrs.addFlashAttribute("successMessage", "입금 확인이 완료되었습니다.");
        return "redirect:/admin/orders/hall";
    }

    /**
     * 주문 취소 처리
     */
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId,
                              RedirectAttributes redirectAttrs) {
        orderService.cancelOrder(orderId);
        redirectAttrs.addFlashAttribute("successMessage", "주문이 취소되었습니다.");
        return "redirect:/admin/orders/hall";
    }

    /* 서빙 처리 */

    /**
     * 개별 아이템 서빙 완료 처리 (AJAX)
     */
    @PostMapping("/{orderId}/items/{orderItemId}/serve")
    @ResponseBody
    public Map<String, Object> serveItem(@PathVariable Long orderId,
                                         @PathVariable Long orderItemId) {
        orderService.markItemAsServed(orderId, orderItemId);
        return Map.of("success", true);
    }

    /**
     * 개별 아이템 서빙 취소 처리 (AJAX)
     */
    @PostMapping("/{orderId}/items/{orderItemId}/unserve")
    @ResponseBody
    public Map<String, Object> unserveItem(@PathVariable Long orderId,
                                           @PathVariable Long orderItemId) {
        orderService.markItemAsUnserved(orderId, orderItemId);
        return Map.of("success", true);
    }

    /**
     * 서빙 완료 처리
     */
    @PostMapping("/{orderId}/complete")
    public String completeServing(@PathVariable Long orderId,
                                  RedirectAttributes redirectAttrs) {
        orderService.completeServing(orderId);
        redirectAttrs.addFlashAttribute("successMessage", "서빙이 완료되었습니다.");
        return "redirect:/admin/orders/hall/serve";
    }

    /* 주문 내역 */

    /**
     * 전체 주문 내역 페이지
     */
    @GetMapping("/history")
    public String historyPage(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("pageTitle", "주문 내역");
        return "admin/orders-history";
    }

}