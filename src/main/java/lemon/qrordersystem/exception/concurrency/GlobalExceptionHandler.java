//package lemon.qrordersystem.exception.concurrency;
//
//import jakarta.persistence.OptimisticLockException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//@ControllerAdvice
//public class GlobalExceptionHandler {
//
//    /**
//     * 낙관적 락 예외 처리
//     * : 다른 사용자가 동시에 장바구니를 수정한 경우
//     */
//    @ExceptionHandler(OptimisticLockException.class)
//    public String handleOptimisticLockException(
//            OptimisticLockException ex,
//            RedirectAttributes redirectAttributes) {
//
//        redirectAttributes.addFlashAttribute("errorMessage",
//                "다른 사용자가 장바구니를 수정하고 있습니다. 다시 시도해주세요.");
//
//        return "redirect:/cart";
//    }
//
//    /**
//     * IllegalStateException 처리
//     * : 주문 중인 장바구니를 수정하려 할 때
//     */
//    @ExceptionHandler(IllegalStateException.class)
//    public String handleIllegalStateException(
//            IllegalStateException ex,
//            RedirectAttributes redirectAttributes) {
//
//        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
//
//        return "redirect:/cart";
//    }
//}