package lemon.qrordersystem.exception;

/**
 * 카테고리 조회 실패 시 발생하는 예외.
 */
public class CategoryNotFoundException extends BusinessException {

    public CategoryNotFoundException(Long id) {
        super(ErrorCode.NOT_FOUND, "존재하지 않는 카테고리입니다. id=" + id);
    }
}