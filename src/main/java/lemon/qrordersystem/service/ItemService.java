package lemon.qrordersystem.service;

import lemon.qrordersystem.dto.ItemSaveRequest;
import lemon.qrordersystem.entity.item.Category;
import lemon.qrordersystem.entity.item.Item;
import lemon.qrordersystem.exception.CategoryNotFoundException;
import lemon.qrordersystem.exception.ItemNotFoundException;
import lemon.qrordersystem.repository.CategoryRepository;
import lemon.qrordersystem.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {
    
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final WebSocketService ws;

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public List<Item> getActiveItems() {
        return itemRepository.findByIsActiveTrue();
    }

    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
    }

    /**
     * 메뉴 등록
     */
    public Long createItem(ItemSaveRequest req) {

        // !! 이름 겹칠 때 예외

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(req.getCategoryId()));

        String desc = normalizeDesc(req);

        Item item = Item.builder()
                .name(req.getName().trim())
                .price(req.getPrice())
                .description(desc)
                .isActive(req.getIsActive())
                .category(category)
                .build();

        Item savedItem = itemRepository.save(item);

        return savedItem.getId();
    }

    /**
     * 메뉴 수정
     */
    public Item updateItem(Long id, ItemSaveRequest req) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(req.getCategoryId()));

        // description 공백 정리
        String desc = normalizeDesc(req);

        item.update(
                req.getName().trim(),
                req.getPrice(),
                category,
                req.getIsActive(),
                desc
        );

        ws.notifyItemUpdatedForCustomers(
                new WebSocketService.ItemUpdatedDto(
                        item.getId(),
                        item.getName(),
                        item.getPrice(),
                        Boolean.TRUE.equals(item.getIsActive()),
                        item.getDescription()
                )
        );
        return item;
    }

    /**
     * 메뉴 삭제
     */
    public void deleteItem(Long id) {
        itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));

        itemRepository.deleteById(id);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    // 아이템 설명 공백 정리
    private static String normalizeDesc(ItemSaveRequest req) {
        String desc = req.getDescription();
        if (desc != null) {
            desc = desc.trim();
            if (desc.isEmpty()) desc = null;
        }
        return desc;
    }
}