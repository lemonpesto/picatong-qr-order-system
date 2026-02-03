package lemon.qrordersystem.controller.admin;

import lemon.qrordersystem.dto.KitchenOrderDto;
import lemon.qrordersystem.entity.order.Order;
import lemon.qrordersystem.entity.order.OrderItem;
import lemon.qrordersystem.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/kitchen/orders")
public class AdminKitchenController {

    private final OrderService orderService;

    /**
     * 주방 페이지
     */
    @GetMapping
    public String kitchenPage(Model model) {
        List<Order> orders = orderService.getKitchenOrders();

        // 제조가 필요한 아이템만 필터링하여 DTO로 변환
        List<KitchenOrderDto> kitchenOrders = orders.stream()
                .map(order -> {
                    List<OrderItem> manufacturedItems = order.getOrderItems().stream()
                            .filter(oi -> oi.getItem().getCategory() != null
                                    && oi.getItem().getCategory().getIsManufactured())
                            .collect(Collectors.toList());

                    return new KitchenOrderDto(
                            order.getId(),
                            order.getTable().getTableNum(),
                            order.getConfirmedAt(),
                            manufacturedItems
                    );
                })
                .filter(dto -> !dto.getManufacturedItems().isEmpty()) // 제조 아이템이 있는 주문만
                .collect(Collectors.toList());

        model.addAttribute("orders", kitchenOrders);
        model.addAttribute("pageTitle", "주방");
        return "admin/orders-kitchen";
    }

    /**
     * 개별 아이템 조리 완료 처리
     */
    @PostMapping("/{orderId}/items/{orderItemId}/cook")
    @ResponseBody
    public Map<String, Object> cookItem(@PathVariable Long orderId,
                                        @PathVariable Long orderItemId) {
        orderService.markItemAsCooked(orderId, orderItemId);
        return Map.of("success", true);
    }

    /**
     * 개별 아이템 조리 취소 처리
     */
    @PostMapping("/{orderId}/items/{orderItemId}/uncook")
    @ResponseBody
    public Map<String, Object> uncookItem(@PathVariable Long orderId,
                                          @PathVariable Long orderItemId) {
        orderService.markItemAsUncooked(orderId, orderItemId);
        return Map.of("success", true);
    }

    /**
     * 전체 조리 완료 처리
     */
    @PostMapping("/{orderId}/complete")
    @ResponseBody
    public Map<String, Object> completeCooking(@PathVariable Long orderId) {
        orderService.completeCooking(orderId);
        return Map.of("success", true, "message", "조리가 완료되었습니다.");
    }
}