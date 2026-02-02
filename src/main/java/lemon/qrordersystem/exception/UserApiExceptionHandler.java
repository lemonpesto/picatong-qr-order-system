//package lemon.qrordersystem.exception;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//@Slf4j
//@RestControllerAdvice(basePackages = "lemon.qrordersystem.controller.user.api")
//public class UserApiExceptionHandler {
//
//    @ExceptionHandler(BusinessException.class)
//    public ResponseEntity<String> handle(BusinessException e) {
//        log.warn("[USER-API] message={}", e.getMessage());
//        return ResponseEntity.badRequest().body(e.getMessage());
//    }
//}
