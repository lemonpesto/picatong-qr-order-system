package lemon.qrordersystem.service;


import lemon.qrordersystem.entity.item.Category;
import lemon.qrordersystem.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 모든 카테고리 조회 (순서대로)
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    /**
     * 카테고리 생성
     */
    public Category createCategory(String name) {
        // 현재 가장 큰 displayOrder 값 찾기
        Integer maxOrder = categoryRepository.findMaxDisplayOrder();
        int nextOrder = (maxOrder == null) ? 0 : maxOrder + 1;

        Category category = Category.builder()
                .name(name)
                .displayOrder(nextOrder)
                .build();

        return categoryRepository.save(category);
    }

    /**
     * 카테고리 삭제
     */
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    /**
     * 카테고리 순서 변경
     */
    public void updateCategoryOrder(List<Long> orderedIds) {
        for (int i = 0; i < orderedIds.size(); i++) {
            Long categoryId = orderedIds.get(i);
            categoryRepository.updateDisplayOrder(categoryId, i);
        }
    }
}
