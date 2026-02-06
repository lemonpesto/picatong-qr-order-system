package lemon.qrordersystem.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lemon.qrordersystem.dto.ApiErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "lemon.qrordersystem.controller.user.api")
public class UserApiExceptionHandler {

    @ExceptionHandler(ItemSoldOutException.class)
    public ResponseEntity<ApiErrorResponse> handleSoldOut(ItemSoldOutException ex, HttpServletRequest req) {
        return json(ErrorCode.SOLD_OUT, ex.getMessage(), req);
    }

    @ExceptionHandler({
            ItemNotFoundException.class,
            CategoryNotFoundException.class,
            OrderNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> handleNotFound(BusinessException ex, HttpServletRequest req) {
        return json(ErrorCode.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {
        return json(ex.getErrorCode(), ex.getMessage(), req);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiErrorResponse> handleIllegalState(RuntimeException ex, HttpServletRequest req) {
        return json(ErrorCode.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ErrorCode.BAD_REQUEST.getDefaultMessage();
        if (ex.getBindingResult().hasErrors() && ex.getBindingResult().getFieldError() != null) {
            msg = ex.getBindingResult().getFieldError().getDefaultMessage();
        }
        return json(ErrorCode.BAD_REQUEST, msg, req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return json(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage(), req);    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleBadJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return json(ErrorCode.BAD_REQUEST, "요청 본문(JSON)이 올바르지 않습니다.", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
        return json(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage(), req);
    }

    private ResponseEntity<ApiErrorResponse> json(ErrorCode errorCode, String msg, HttpServletRequest req) {
        ApiErrorResponse body = ApiErrorResponse.of(errorCode, msg, req.getRequestURI());
        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }
}
