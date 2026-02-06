package lemon.qrordersystem.exception;

/**
 * 비즈니스 규칙 위반 시 발생하는 기본 예외.
 * 모든 도메인 예외의 부모로 사용.
 */
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(String message) {
        super(message);
        this.errorCode = ErrorCode.BAD_REQUEST;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
