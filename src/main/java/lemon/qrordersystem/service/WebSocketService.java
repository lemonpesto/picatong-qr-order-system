package lemon.qrordersystem.service;

import lemon.qrordersystem.dto.CartSyncDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    // ==== admin =====
    private static final String TOPIC_ADMIN_CONFIRM = "/topic/admin/orders/hall";
    private static final String TOPIC_ADMIN_SERVE = "/topic/admin/orders/hall/serve";
    private static final String TOPIC_ADMIN_KITCHEN = "/topic/admin/kitchen/orders";
    private static final String TOPIC_ITEMS_UPDATED = "/topic/items/updated";

    // ==== user ====
    private String topicTableCart(Long tableId) {
        return "/topic/tables/" + tableId + "/cart";
    }

    private String topicTableNav(Long tableId) {
        return "/topic/tables/" + tableId + "/nav";
    }

    private String topicTablePayment(Long tableId) {
        return "/topic/tables/" + tableId + "/payment";
    }

    // ==== order progress (부분 갱신) =====
    private String topicOrderProgress(Long orderItemId) {
        return "/topic/orders/" + orderItemId + "/progress";
    }

    // ==== 동일 페이지 내 실시간 동기화용 ====
    private String topicKitchenProgress(Long orderItemId) {
        return "/topic/kitchen/orders/" + orderItemId + "/progress";
    }
    private String topicServeProgress(Long orderItemId) {
        return "/topic/serve/orders/" + orderItemId + "/progress";
    }
    private String topicKitchenComplete(Long orderId) {
        return "/topic/kitchen/orders/" + orderId + "/complete";
    }
    private String topicServeComplete(Long orderId) {
        return "/topic/serve/orders/" + orderId + "/complete";
    }

    private void sendAfterCommit(Runnable sendAction) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            sendAction.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendAction.run();
            }
        });
    }

    // ===== Admin reload signals =====
    public void adminConfirmReload() {
        sendAfterCommit(() -> messagingTemplate.convertAndSend(
                TOPIC_ADMIN_CONFIRM,
                new SimpleEvent("RELOAD", null)));
    }

    public void adminServeReload() {
        sendAfterCommit(() -> messagingTemplate.convertAndSend(
                TOPIC_ADMIN_SERVE,
                new SimpleEvent("RELOAD", null)));
    }

    public void adminKitchenReload() {
        sendAfterCommit(() -> messagingTemplate.convertAndSend(
                TOPIC_ADMIN_KITCHEN,
                new SimpleEvent("RELOAD", null)));
    }

    // ===== Table signals =====
    public void tableCartUpdated(Long tableId, lemon.qrordersystem.dto.CartSyncDto payload) {
        sendAfterCommit(() -> messagingTemplate.convertAndSend(
                topicTableCart(tableId),
                new CartEvent("CART_UPDATED", payload)));
    }

    public void tableRedirect(Long tableId, String url, String message) {
        sendAfterCommit(() -> messagingTemplate.convertAndSend(
                topicTableNav(tableId),
                new SimpleEvent("REDIRECT", new RedirectPayload(url, message))));
    }

    public void tableInfo(Long tableId, String message) {
        sendAfterCommit(() -> messagingTemplate.convertAndSend(
                topicTableNav(tableId),
                new SimpleEvent("INFO", new MessagePayload(message))));
    }

    public void paymentRedirect(Long tableId, Long orderId, String url, String message) {
        sendAfterCommit(() -> messagingTemplate.convertAndSend(
                topicTablePayment(tableId),
                new PaymentRedirectEvent(orderId, tableId, "REDIRECT", url, message)));
    }

    // ==== 고객 전체 Signal ====
    public void notifyItemUpdatedForCustomers(ItemUpdatedDto dto) {
        sendAfterCommit(() -> messagingTemplate.convertAndSend(
                TOPIC_ITEMS_UPDATED,
                new SimpleEvent<>("ITEM_UPDATED", dto)));
    }

    // ===== Order progress (부분 업데이트 유지) =====
    public void orderItemProgress(Long orderId, Long orderItemId, boolean checked) {
        sendAfterCommit(() -> messagingTemplate.convertAndSend(
                topicOrderProgress(orderItemId),
                new OrderItemProgressEvent(orderId, orderItemId, checked)));
    }

    /**
     * 주방 페이지 내 조리 진행상황 실시간 동기화
     */
    public void kitchenItemProgress(Long orderId, Long orderItemId, boolean checked) {
        sendAfterCommit(() -> messagingTemplate.convertAndSend(
                topicKitchenProgress(orderItemId),
                new OrderItemProgressEvent(orderId, orderItemId, checked)));
    }

    /**
     * 서빙 페이지 내 서빙 진행상황 실시간 동기화
     */
    public void orderItemServeProgress(Long orderId, Long orderItemId, boolean checked) {
        sendAfterCommit(() -> messagingTemplate.convertAndSend(
                topicServeProgress(orderItemId),
                new OrderItemProgressEvent(orderId, orderItemId, checked)));
    }

    /**
     * 주방 페이지 내 전체 조리 완료 실시간 동기화
     */
    public void kitchenOrderComplete(Long orderId) {
        sendAfterCommit(() -> messagingTemplate.convertAndSend(
                topicKitchenComplete(orderId),
                new SimpleEvent("COMPLETED", new OrderCompletePayload(orderId))));
    }

    /**
     * 서빙 페이지 내 전체 서빙 완료 실시간 동기화
     */
    public void serveOrderComplete(Long orderId) {
        sendAfterCommit(() -> messagingTemplate.convertAndSend(
                topicServeComplete(orderId),
                new SimpleEvent("COMPLETED", new OrderCompletePayload(orderId))));
    }

    // ===== DTOs =====
    public record SimpleEvent<T>(String type, T data) {
    }

    public record RedirectPayload(String url, String message) {
    }

    public record MessagePayload(String message) {
    }

    public record CartEvent(String type, CartSyncDto data) {
    }

    public record PaymentRedirectEvent(Long orderId, Long tableId, String type, String url, String message) {
    }

    public record OrderItemProgressEvent(Long orderId, Long orderItemId, boolean checked) {
    }

    public record OrderCompletePayload(Long orderId) {}

    public record ItemUpdatedDto(Long itemId, String name, int price, boolean isActive, String description) {
    }
}