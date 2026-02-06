package lemon.qrordersystem.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;

@Slf4j
@ControllerAdvice(basePackages = {
        "lemon.qrordersystem.controller.user",
        "lemon.qrordersystem.controller.user.page"
})
public class UserPageExceptionHandler {

    // ===== 도메인 예외 =====

    @ExceptionHandler(ItemSoldOutException.class)
    public String handleSoldOut(ItemSoldOutException ex,
                                HttpServletRequest req,
                                RedirectAttributes redirectAttrs) {

        redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());

        return "redirect:" + pickSafeRedirectTarget(req, "/items");
    }

    @ExceptionHandler({
            ItemNotFoundException.class,
            CategoryNotFoundException.class,
            OrderNotFoundException.class
    })
    public String handleNotFound(BusinessException ex,
                                 HttpServletRequest req,
                                 RedirectAttributes redirectAttrs) {

        redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:" + pickSafeRedirectTarget(req, "/items");
    }

    @ExceptionHandler(IncorrectResultSizeDataAccessException.class)
    public String handleNonUnique(IncorrectResultSizeDataAccessException ex,
                                  HttpServletRequest req,
                                  RedirectAttributes redirectAttrs) {

        log.warn("[USER PAGE] non-unique result. path={}", req.getRequestURI(), ex);

        redirectAttrs.addFlashAttribute("errorMessage",
                "장바구니 상태가 꼬여 자동으로 정리했습니다. 다시 시도해 주세요.");

        return "redirect:/cart";
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex,
                                 HttpServletRequest req,
                                 RedirectAttributes redirectAttrs) {

        redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:" + pickSafeRedirectTarget(req, "/items");
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public String handleIllegalState(RuntimeException ex,
                                     HttpServletRequest req,
                                     RedirectAttributes redirectAttrs) {

        redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:" + pickSafeRedirectTarget(req, "/items");
    }


    // ===== 예상 못한 예외 =====

    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception ex,
                                   HttpServletRequest req,
                                   RedirectAttributes redirectAttrs) {

        redirectAttrs.addFlashAttribute("errorMessage", "요청 처리 중 오류가 발생했습니다.");
        return "redirect:" + pickSafeRedirectTarget(req, "/items");
    }

    /**
     * redirect 우선순위
     * 1) returnUrl 파라미터 (/items, /cart 같은 상대경로만 허용)
     * 2) Referer (같은 방식으로 상대경로 추출)
     * 3) fallback: 요청 URI 기반으로 대충 안전한 곳 (/cart or /items)
     */
    private String pickSafeRedirectTarget(HttpServletRequest req, String defaultTarget) {
        String returnUrl = sanitizeRelativeUrl(req.getParameter("returnUrl"));
        if (returnUrl != null) return returnUrl;

        String referer = sanitizeRefererToRelativeUrl(req.getHeader("Referer"));
        if (referer != null) return referer;

        String uri = req.getRequestURI();
        if (uri != null) {
            if (uri.startsWith("/payments") || uri.startsWith("/orders")) return "/cart";
            if (uri.startsWith("/cart")) return "/cart";
        }
        return defaultTarget;
    }

    private String sanitizeRelativeUrl(String url) {
        if (!StringUtils.hasText(url)) return null;
        String u = url.trim();

        // 상대경로만 허용 (오픈 리다이렉트 방지)
        if (!u.startsWith("/")) return null;
        if (u.startsWith("//")) return null;
        if (u.contains("://")) return null;

        return u;
    }

    private String sanitizeRefererToRelativeUrl(String referer) {
        if (!StringUtils.hasText(referer)) return null;
        try {
            URI uri = URI.create(referer);
            String path = uri.getPath();
            if (!StringUtils.hasText(path)) return null;

            String query = uri.getQuery();
            String candidate = (StringUtils.hasText(query)) ? (path + "?" + query) : path;
            return sanitizeRelativeUrl(candidate);
        } catch (Exception e) {
            return null;
        }
    }
}
