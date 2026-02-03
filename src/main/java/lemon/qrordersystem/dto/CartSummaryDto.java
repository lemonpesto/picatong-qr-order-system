package lemon.qrordersystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartSummaryDto {
    private Integer totalQuantity;
    private Integer totalAmount;
}
