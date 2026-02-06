package lemon.qrordersystem.exception;

/**
 * 주문 조회 실패 시 발생하는 예외.
 */
public class OrderNotFoundException extends BusinessException {

    public OrderNotFoundException(Long id) {
        super(ErrorCode.NOT_FOUND, "존재하지 않는 주문입니다. id=" + id);
    }
}
