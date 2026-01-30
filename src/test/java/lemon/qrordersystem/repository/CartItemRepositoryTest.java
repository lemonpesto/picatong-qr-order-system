package lemon.qrordersystem.repository;

import jakarta.persistence.EntityManager;
import lemon.qrordersystem.entity.cart.Cart;
import lemon.qrordersystem.entity.cart.CartItem;
import lemon.qrordersystem.entity.item.Category;
import lemon.qrordersystem.entity.item.Item;
import lemon.qrordersystem.entity.table.TableEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class CartItemRepositoryTest {

    @Autowired
    CartItemRepository cartItemRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    TableRepository tableRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("cartId로 장바구니 아이템 목록 조회")
    void findByCartId() {
        // given
        Cart cart = createCart();
        Item item1 = createItem("콜라");
        Item item2 = createItem("사이다");

        createCartItem(cart, item1, 2);
        createCartItem(cart, item2, 1);

        em.flush();
        em.clear();

        // when
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        // then
        assertThat(cartItems.size()).isEqualTo(2);
        assertThat(cartItems)
                .extracting(ci -> ci.getItem().getName())
                .containsExactlyInAnyOrder("콜라", "사이다");
    }

    private Cart createCart() {

        TableEntity table = tableRepository.findByTableNum(1)
                .orElseThrow(() -> new IllegalArgumentException("테이블 없음"));;
        Cart cart = Cart.builder()
                .table(table)
                .build();
        em.persist(cart);
        return cart;
    }

    private Item createItem(String name) {
        Category drinkCategory = categoryRepository.findByName("음료")
                .orElseThrow(() -> new IllegalArgumentException("카테고리 없음"));

        Item item = Item.builder()
                .category(drinkCategory)
                .name(name)
                .price(5000)
                .isActive(true)
                .build();
        em.persist(item);
        return item;
    }

    private CartItem createCartItem(Cart cart, Item item, int quantity) {
        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .item(item)
                .count(quantity)
                .build();
        em.persist(cartItem);
        return cartItem;
    }


}