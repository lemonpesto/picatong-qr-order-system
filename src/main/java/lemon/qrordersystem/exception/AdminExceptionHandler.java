package lemon.qrordersystem.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.util.Map;

@Slf4j
@ControllerAdvice(basePackages = "lemon.qrordersystem.controller.admin")
public class AdminExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(BusinessException e,
                                          HttpServletRequest request,
                                          RedirectAttributes redirectAttrs) {

        if (isAjaxRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }

        redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:" + resolveRedirectTarget(request);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public Object handleIllegalState(RuntimeException e,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttrs) {

        if (isAjaxRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }

        redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:" + resolveRedirectTarget(request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Object handleTypeMismatch(MethodArgumentTypeMismatchException e,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttrs) {

        if (isAjaxRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "잘못된 요청입니다."));
        }

        redirectAttrs.addFlashAttribute("errorMessage", "잘못된 요청입니다.");
        return "redirect:" + resolveRedirectTarget(request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidation(MethodArgumentNotValidException e,
                                   HttpServletRequest request,
                                   RedirectAttributes redirectAttrs) {

        String msg = "입력값이 올바르지 않습니다.";
        if (e.getBindingResult().hasErrors() && e.getBindingResult().getFieldError() != null) {
            msg = e.getBindingResult().getFieldError().getDefaultMessage();
        }

        if (isAjaxRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", msg));
        }

        redirectAttrs.addFlashAttribute("errorMessage", msg);
        return "redirect:" + resolveRedirectTarget(request);
    }

    @ExceptionHandler(Exception.class)
    public Object handleUnexpected(Exception e,
                                   HttpServletRequest request,
                                   RedirectAttributes redirectAttrs) {

        log.error("[ADMIN] Unexpected error. uri={}, method={}",
                safeUri(request), request.getMethod(), e);

        if (isAjaxRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "서버 오류가 발생했습니다."));
        }

        redirectAttrs.addFlashAttribute("errorMessage", "서버 오류가 발생했습니다.");
        return "redirect:" + resolveRedirectTarget(request);
    }

    // ===========================
    // Helpers
    // ===========================

    private boolean isAjaxRequest(HttpServletRequest request) {
        String xhr = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(xhr)
                || (accept != null && accept.contains("application/json"));
    }

    private String resolveRedirectTarget(HttpServletRequest request) {
        String contextPath = request.getContextPath() == null ? "" : request.getContextPath();

        // 1) referer가 /admin/* 이면 그쪽으로 (탭 유지)
        String referer = request.getHeader("Referer");
        String fromReferer = safeAdminPathFromReferer(referer, contextPath);
        if (fromReferer != null) return fromReferer;

        // 2) fallback
        String uri = safeUri(request);

        if (uri.startsWith(contextPath + "/admin/kitchen")) {
            return contextPath + "/admin/kitchen/orders";
        }
        if (uri.startsWith(contextPath + "/admin/orders")) {
            if (uri.contains("/history")) return contextPath + "/admin/orders/history";
            if (uri.contains("/hall/serve")) return contextPath + "/admin/orders/hall/serve";
            return contextPath + "/admin/orders/hall";
        }
        return contextPath + "/admin/items";
    }

    private String safeAdminPathFromReferer(String referer, String contextPath) {
        if (referer == null || referer.isBlank()) return null;

        try {
            URI u = URI.create(referer);
            String path = u.getPath();
            String query = u.getQuery();
            if (path == null) return null;

            // open redirect 방지: /admin 경로만 허용
            if (!path.startsWith(contextPath + "/admin/")) return null;

            return (query == null || query.isBlank()) ? path : (path + "?" + query);
        } catch (Exception ignore) {
            return null;
        }
    }

    private String safeUri(HttpServletRequest request) {
        try {
            return request.getRequestURI() == null ? "" : request.getRequestURI();
        } catch (Exception e) {
            return "";
        }
    }
}
