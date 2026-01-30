package lemon.qrordersystem.entity.cart;

public enum CartStatus {
    ACTIVE, // 담는 중
    ORDERING, // 주문 중
    COMPLETED, // 송금 후
    CANCELED // 취소된 주문
}
