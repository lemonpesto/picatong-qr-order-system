package lemon.qrordersystem.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@ControllerAdvice(basePackages = "lemon.qrordersystem.controller.user.page")
public class UserPageExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String handle(BusinessException e,
                                          HttpServletRequest request,
                                          RedirectAttributes redirectAttrs) {
        redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());

        String uri = request.getRequestURI();
        log.warn("[USER-PAGE] uri={}, message={}", uri, e.getMessage());

        // !! 수정
        if (uri.startsWith("/cart")) return "redirect:/cart";
        if (uri.startsWith("/payment")) return "redirect:/payment";
        if (uri.startsWith("/orders")) return "redirect:/orders";
        return "redirect:/items";

    }
}
