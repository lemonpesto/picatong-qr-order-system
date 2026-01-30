package lemon.qrordersystem.entity;

public enum OrderStatus {

    PAYMENT_PENDING("입금 중"),
    PAYMENT_CONFIRM_WAITING("입금 확인 대기 중"),
    COOKING("조리 중"),
    SERVING("서빙 중"),
    COMPLETED("서빙 완료"),
    CANCELLED("취소됨");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
