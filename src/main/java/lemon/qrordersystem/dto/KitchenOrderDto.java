package lemon.qrordersystem.dto;

import lemon.qrordersystem.entity.order.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KitchenOrderDto {
    private Long orderId;
    private int tableNum;
    private LocalDateTime confirmedAt;
    private List<OrderItem> manufacturedItems; // 이미 필터링된 제조 아이템만
}
