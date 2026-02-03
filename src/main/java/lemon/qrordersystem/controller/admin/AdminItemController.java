package lemon.qrordersystem.controller.admin;

import lemon.qrordersystem.dto.ItemSaveRequest;
import lemon.qrordersystem.entity.item.Category;
import lemon.qrordersystem.entity.item.Item;
import lemon.qrordersystem.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/items")
@RequiredArgsConstructor
public class AdminItemController {
    
    private final ItemService itemService;

    /**
     * 메뉴 관리
     */
    @GetMapping
    public String item(Model model) {
        List<Item> items = itemService.getAllItems();
        List<Category> categories = itemService.getAllCategories();
        model.addAttribute("items", items);
        model.addAttribute("categories", categories);
        model.addAttribute("pageTitle", "메뉴관리");
        return "admin/items";
    }

    /**
     * 새 아이템 등록 폼
     */
    @GetMapping("/new")
    public String itemNew(Model model) {
        List<Category> categories = itemService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("pageTitle", "메뉴관리");
        return "admin/items-new";
    }

    /**
     * 새 아이템 등록 처리
     */
    @PostMapping("/new")
    public String createItem(@ModelAttribute ItemSaveRequest req,
                             RedirectAttributes redirectAttrs) {
        itemService.createItem(req);
        redirectAttrs.addFlashAttribute("successMessage", "등록이 완료되었습니다.");
        return "redirect:/admin/items";
    }

    /**
     * 아이템 수정 폼
     */
    @GetMapping("/{id}/edit")
    public String itemEdit(@PathVariable Long id, Model model) {
        Item item = itemService.getItemById(id);
        List<Category> categories = itemService.getAllCategories();
        model.addAttribute("item", item);
        model.addAttribute("categories", categories);
        model.addAttribute("pageTitle", "메뉴관리");
        return "admin/items-edit";
    }

    /**
     * 아이템 수정 처리
     */
    @PostMapping("/{id}")
    public String updateItem(@PathVariable Long id,
                             @ModelAttribute ItemSaveRequest req,
                             RedirectAttributes redirectAttrs) {

        itemService.updateItem(id, req);
        redirectAttrs.addFlashAttribute("successMessage", "수정이 완료되었습니다");
        return "redirect:/admin/items";
    }

    /**
     * 아이템 삭제 처리
     */
    @PostMapping("/{id}/delete")
    public String deleteItem(@PathVariable Long id,
                             RedirectAttributes redirectAttrs) {
        itemService.deleteItem(id);
        redirectAttrs.addFlashAttribute("successMessage", "삭제가 완료되었습니다");
        return "redirect:/admin/items";
    }
}
