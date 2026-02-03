package lemon.qrordersystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartUpdatedMessage {
    private String type; // "cartUpdated"
    private CartSummaryDto summary;
}
