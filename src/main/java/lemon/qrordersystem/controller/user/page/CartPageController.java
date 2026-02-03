package lemon.qrordersystem.controller.user.page;

import lemon.qrordersystem.dto.CartSummaryDto;
import lemon.qrordersystem.entity.cart.Cart;
import lemon.qrordersystem.entity.cart.CartItem;
import lemon.qrordersystem.entity.cart.CartStatus;
import lemon.qrordersystem.security.CustomUserDetails;
import lemon.qrordersystem.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CartPageController {

    private final CartService cartService;

    /**
     * 장바구니 페이지
     */
    @GetMapping("/cart")
    public String cart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Long tableId = userDetails.getId();

        // 장바구니 상태 확인
        Cart cart = cartService.getOrCreateCart(tableId);
        boolean isOrdering = (cart.getStatus() == CartStatus.ORDERING);

        // 장바구니 아이템과 요약 정보
        List<CartItem> cartItems = cartService.getCartItems(userDetails.getId());
        CartSummaryDto summary = cartService.getCartSummary(userDetails.getId());

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("summary", summary);
        model.addAttribute("isOrdering", isOrdering);

        model.addAttribute("navTitle", "장바구니");
        model.addAttribute("navBack", "/items");
        model.addAttribute("returnUrl", "/cart");
        model.addAttribute("tableNum", userDetails.getTableNum());

        return "/user/cart";
    }
}
