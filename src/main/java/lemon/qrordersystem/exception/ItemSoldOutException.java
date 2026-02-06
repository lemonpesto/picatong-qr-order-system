package lemon.qrordersystem.exception;

public class ItemSoldOutException extends BusinessException {

    public ItemSoldOutException(Long id) {
        super(ErrorCode.SOLD_OUT, "품절된 아이템입니다. id=" + id);
    }
}
