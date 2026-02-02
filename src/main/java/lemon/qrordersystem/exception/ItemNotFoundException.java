package lemon.qrordersystem.exception;

/**
 * 아이템 조회 실패 시 발생하는 예외.
 */
public class ItemNotFoundException extends BusinessException {

    public ItemNotFoundException(Long id) {
        super("존재하지 않는 아이템입니다. id=" + id);
    }
}
