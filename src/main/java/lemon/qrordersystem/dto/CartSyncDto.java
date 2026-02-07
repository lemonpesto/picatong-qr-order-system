package lemon.qrordersystem.dto;

import java.util.List;

public record CartSyncDto(
        CartSummaryDto summary,
        List<CartLineDto> items,
        boolean ordering) {

    public record CartLineDto(
            Long itemId,
            String name,
            int unitPrice,
            int quantity,
            int lineAmount
    ) {
    }
}