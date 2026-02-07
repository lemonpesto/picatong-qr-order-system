package lemon.qrordersystem.service;

import lemon.qrordersystem.entity.cart.Cart;
import lemon.qrordersystem.entity.cart.CartItem;
import lemon.qrordersystem.entity.item.Item;
import lemon.qrordersystem.entity.order.Order;
import lemon.qrordersystem.entity.order.OrderItem;
import lemon.qrordersystem.entity.order.OrderStatus;
import lemon.qrordersystem.exception.BusinessException;
import lemon.qrordersystem.exception.OrderNotFoundException;
import lemon.qrordersystem.repository.OrderItemRepository;
import lemon.qrordersystem.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final WebSocketService ws;


    // ============================================================================
    // 주문 생성
    // ============================================================================

    /**
     * 주문 생성
     * - 장바구니 잠금
     */
    @Transactional
    public Order createOrder(Long tableId) {
        // 장바구니 가져오기 (장바구니 비어 있으면 주문 생성 불가)
        Cart cart = cartService.getOrCreateCart(tableId);
        List<CartItem> cartItems = cart.getCartItems();

        if (cartItems.isEmpty()) {
            throw new BusinessException("장바구니가 비어 있습니다.");
        }

        // 장바구니 잠금
        cart.lockForOrdering();

        // 주문 생성
        Order order = Order.builder()
                .table(cart.getTable())
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();

        int totalAmount = 0;
        int totalQuantity = 0;

        // 장바구니 아이템 -> 주문 아이템 변환
        for (CartItem ci : cartItems) {
            Item item = ci.getItem();
            int price = item.getPrice();
            int qty = ci.getQuantity();

            totalAmount += price * qty;
            totalQuantity += qty;

            // 주문 아이템 생성 및 추가
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .item(item)
                    .orderPrice(price)
                    .quantity(qty)
                    .build();

            order.addItem(orderItem);
        }

        order.setTotals(totalAmount, totalQuantity);
        Order savedOrder = orderRepository.save(order);

        // 트랜잭션 커밋 후 WebSocket 알림 (sendAfterCommit 사용)
        ws.adminConfirmReload();
        ws.tableRedirect(tableId, "/items", "같은 테이블에서 주문이 시작되어 장바구니 접근이 제한됩니다.");

        return savedOrder;
    }

    @Transactional
    public Order getOrCreatePaymentPendingOrder(Long tableId) {

        // 1) 이미 미결제 주문이 있으면 새로 만들지 말고 그대로 반환
        return orderRepository
                .findFirstByTable_IdAndStatusOrderByCreatedAtDesc(tableId, OrderStatus.PAYMENT_PENDING)
                .orElseGet(() -> createOrder(tableId));
    }

    // ============================================================================
    // 고객 측 주문 처리
    // ============================================================================

    /**
     * 입금 완료
     * - 주문 상태: '입금 확인 대기 중'으로 변경
     * - 장바구니 잠금 해제
     */
    @Transactional
    public void requestPaymentConfirm(Long orderId, Long tableId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // 권한 체크
        if (!order.getTable().getId().equals(tableId)) {
            throw new BusinessException("다른 테이블의 주문입니다.");
        }

        order.requestPaymentConfirm();

        // 트랜잭션 커밋 후 WebSocket 알림
        ws.adminConfirmReload();
    }

    /**
     * 주문 취소
     * - 주문 삭제
     */
    @Transactional
    public void deleteOrder(Long orderId, Long tableId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // 권한 체크
        if (!order.getTable().getId().equals(tableId)) {
            throw new BusinessException("다른 테이블의 주문입니다.");
        }

        // 상태 체크: PAYMENT_PENDING 상태만 삭제 가능
        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new BusinessException("취소할 수 없는 주문입니다.");
        }

        // 주문 삭제
        orderRepository.delete(order);
        log.info("주문 삭제: orderId={}, tableId={}", orderId, tableId);

        // 트랜잭션 커밋 후 WebSocket 알림
        ws.adminConfirmReload();
        ws.tableInfo(tableId, "주문이 취소되었습니다.");
    }

    // ============================================================================
    // 관리자 측 주문 처리
    // ============================================================================

    /**
     * 입금 확인
     * - 주문 상태 '조리 중'으로 변경
     * - 장바구니 잠금 해제
     * - 장바구니 비우기
     */
    @Transactional
    public void confirmPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        Long tableId = order.getTable().getId();

        order.confirmPayment();
        cartService.unlockCartToActive(tableId);
        cartService.clearCart(tableId);

        // 트랜잭션 커밋 후 WebSocket 알림
        ws.adminServeReload();
        ws.adminKitchenReload();
        ws.paymentRedirect(tableId, orderId, "/payments/confirm/success", "입금이 확인되었습니다.");
    }

    /**
     * 주문 취소
     * - 주문 상태 CANCELLED로 변경
     * - 장바구니 잠금 해제
     * - 장바구니 비우기
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        Long tableId = order.getTable().getId();

        order.cancel();
        cartService.unlockCartToActive(tableId);
        cartService.clearCart(tableId);

        // 트랜잭션 커밋 후 WebSocket 알림
        ws.paymentRedirect(tableId, orderId, "/payments/confirm/cancel", "관리자에 의해 주문이 취소되었습니다.");
        ws.tableInfo(tableId, "주문이 취소되었습니다.");
    }

    // ============================================================================
    // 주방 전용 주문 처리 메서드
    // ============================================================================

    /**
     * 개별 아이템 조리 완료 처리
     */
    @Transactional
    public void markItemAsCooked(Long orderId, Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findByIdAndOrder_Id(orderItemId, orderId)
                .orElseThrow(() -> new BusinessException("아이템을 찾을 수 없습니다."));

        if (orderItem.getCooked()) return;

        orderItem.markAsCooked();
        log.info("아이템 조리 완료: orderId={}, itemId={}", orderId, orderItemId);

        // 트랜잭션 커밋 후 WebSocket 알림
        ws.orderItemProgress(orderId, orderItemId, true);      // 주방->서빙 간 동기화
        ws.kitchenItemProgress(orderId, orderItemId, true);    // 주방 페이지 내 동기화
    }

    /**
     * 개별 아이템 조리 취소
     */
    @Transactional
    public void markItemAsUncooked(Long orderId, Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findByIdAndOrder_Id(orderItemId, orderId)
                .orElseThrow(() -> new BusinessException("아이템을 찾을 수 없습니다."));

        orderItem.markAsUncooked();
        log.info("아이템 조리 취소: orderId={}, itemId={}", orderId, orderItemId);

        // 트랜잭션 커밋 후 WebSocket 알림
        ws.orderItemProgress(orderId, orderItemId, false);     // 주방->서빙 간 동기화
        ws.kitchenItemProgress(orderId, orderItemId, false);   // 주방 페이지 내 동기화
    }

    /**
     * 전체 아이템 조리 완료 처리
     */
    @Transactional
    public void completeCooking(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // COOKING 상태일 때만 SERVING으로 변경 (이미 SERVING이면 스킵)
        if (order.getStatus() != OrderStatus.COOKING) {
            return;
        }

        // 모든 아이템을 조리 완료로 표시
        order.getOrderItems().forEach(OrderItem::markAsCooked);
        order.completeCooking();

        log.info("전체 조리 완료: orderId={}, status={}", orderId, order.getStatus());

        // 트랜잭션 커밋 후 WebSocket 알림
        ws.adminServeReload();
        ws.kitchenOrderComplete(orderId);  // 주방 페이지 내 동기화
    }

    // ============================================================================
    // 서빙 전용 주문 처리 메서드
    // ============================================================================

    /**
     * 개별 아이템 서빙 완료 처리
     */
    @Transactional
    public void markItemAsServed(Long orderId, Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findByIdAndOrder_Id(orderItemId, orderId)
                .orElseThrow(() -> new BusinessException("아이템을 찾을 수 없습니다."));

        if (orderItem.getServed()) return;

        orderItem.markAsServed();
        log.info("아이템 서빙 완료: orderId={}, itemId={}", orderId, orderItemId);

        // 트랜잭션 커밋 후 WebSocket 알림
        ws.orderItemServeProgress(orderId, orderItemId, true);  // 서빙 페이지 내 동기화
    }

    /**
     * 개별 아이템 서빙 취소
     */
    @Transactional
    public void markItemAsUnserved(Long orderId, Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findByIdAndOrder_Id(orderItemId, orderId)
                .orElseThrow(() -> new BusinessException("아이템을 찾을 수 없습니다."));

        orderItem.markAsUnserved();
        log.info("아이템 서빙 취소: orderId={}, itemId={}", orderId, orderItemId);

        // 트랜잭션 커밋 후 WebSocket 알림
        ws.orderItemServeProgress(orderId, orderItemId, false); // 서빙 페이지 내 동기화
    }

    /**
     * 전체 아이템 서빙 완료 (SERVING -> COMPLETED)
     */
    @Transactional
    public void completeServing(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.completeServing();

        // 트랜잭션 커밋 후 WebSocket 알림
        ws.serveOrderComplete(orderId); // 서빙 페이지 내 동기화
    }

    // ============================================================================
    // 조회 메서드
    // ============================================================================

    @Transactional(readOnly = true)
    public List<Order> getConfirmOrders() {
        return orderRepository.findByStatusInOrderByCreatedAtAsc(
                List.of(OrderStatus.PAYMENT_PENDING, OrderStatus.PAYMENT_CONFIRM_WAITING)
        );
    }

    @Transactional(readOnly = true)
    public List<Order> getKitchenOrders() {
        return orderRepository.findByStatusOrderByConfirmedAtAsc(OrderStatus.COOKING);
    }

    @Transactional(readOnly = true)
    public List<Order> getServeOrders() {
        return orderRepository.findByStatusInOrderByCreatedAtAsc(
                List.of(OrderStatus.COOKING, OrderStatus.SERVING)
        );
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByTable(Integer tableNum) {
        return orderRepository.findByTable_TableNumOrderByCreatedAtDesc(tableNum);
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }


}