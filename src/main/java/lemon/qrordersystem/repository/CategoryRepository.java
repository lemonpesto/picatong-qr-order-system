package lemon.qrordersystem.repository;

import lemon.qrordersystem.entity.item.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByOrderByDisplayOrderAsc();
    Optional<Category> findByName(String name);

    @Query("SELECT MAX(c.displayOrder) FROM Category c")
    Integer findMaxDisplayOrder();

    @Modifying
    @Query("UPDATE Category c SET c.displayOrder = :order WHERE c.id = :id")
    void updateDisplayOrder(@Param("id") Long id, @Param("order") Integer order);
}
