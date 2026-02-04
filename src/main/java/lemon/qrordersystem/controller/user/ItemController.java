package lemon.qrordersystem.controller.user;

import lemon.qrordersystem.entity.item.Category;
import lemon.qrordersystem.entity.item.Item;
import lemon.qrordersystem.security.CustomUserDetails;
import lemon.qrordersystem.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {
    
    private final ItemService itemService;
    
    @GetMapping("/items")
    public String menu(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        List<Item> items = itemService.getAllItems();
        List<Category> categories = itemService.getAllCategories();
        
        model.addAttribute("items", items);
        model.addAttribute("categories", categories);
        model.addAttribute("tableId", userDetails.getId());
        model.addAttribute("tableNum", userDetails.getTableNum());
        model.addAttribute("returnUrl", "/items");
        model.addAttribute("showHistory", true);
        
        return "user/items";
    }
}
