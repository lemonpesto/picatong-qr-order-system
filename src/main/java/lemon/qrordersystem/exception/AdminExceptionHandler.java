//package lemon.qrordersystem.exception;
//
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
///**
// * 관리자 폼 기반 컨트롤러에서 발생하는 예외를
// * Flash Attribute로 전달하여 토스트로 표시.
// *
// * BusinessException 하위의 모든 예외를 단일 핸들러로 캐치.
// * redirect 경로는 요청 URL로부터 자동 결정.
// */
//@ControllerAdvice(basePackages = "lemon.qrordersystem.controller.admin")
//public class AdminExceptionHandler {
//
//    @ExceptionHandler(BusinessException.class)
//    public String handleBusinessException(BusinessException e,
//                                          HttpServletRequest request,
//                                          RedirectAttributes redirectAttrs) {
//        redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
//
//        String uri = request.getRequestURI();
//
//        // 요청 경로에 따라 적절한 페이지로 redirect
//        if (uri.startsWith("/admin/kitchen")) {
//            return "redirect:/admin/kitchen/orders";
//        } else if (uri.startsWith("/admin/orders")) {
//            return "redirect:/admin/orders/hall";
//        } else {
//            return "redirect:/admin/items";
//        }
//    }
//}
