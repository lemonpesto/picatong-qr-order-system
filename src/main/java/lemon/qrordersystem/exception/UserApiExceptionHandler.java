package lemon.qrordersystem.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lemon.qrordersystem.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "lemon.qrordersystem.controller.user.api")
public class UserApiExceptionHandler {

    @ExceptionHandler(ItemSoldOutException.class)
    public ResponseEntity<ApiErrorResponse> handleSoldOut(ItemSoldOutException ex, HttpServletRequest req) {
        return json(ex, req, HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({
            ItemNotFoundException.class,
            CategoryNotFoundException.class,
            OrderNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> handleNotFound(BusinessException ex, HttpServletRequest req) {
        return json(ex, req, HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {
        return json(ex, req, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleBadJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return json(ex, req, HttpStatus.BAD_REQUEST, "요청 본문(JSON)이 올바르지 않습니다.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("[USER API] unexpected error. path={}", req.getRequestURI(), ex);
        return json(ex, req, HttpStatus.INTERNAL_SERVER_ERROR, "요청 처리 중 오류가 발생했습니다.");
    }

    private ResponseEntity<ApiErrorResponse> json(Exception ex, HttpServletRequest req, HttpStatus status, String msg) {
        ApiErrorResponse body = ApiErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                msg,
                req.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
