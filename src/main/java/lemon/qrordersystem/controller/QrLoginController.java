package lemon.qrordersystem.controller;

import lemon.qrordersystem.security.QrAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping("/qr")
@AllArgsConstructor
public class QrLoginController {

    private final QrAuthService qrAuthService;

    // 에러 페이지
    @GetMapping
    public String entry() {
        // "QR로 접속해주세요" 같은 안내 페이지
        return "qr/entry.html";
    }

    // 관리자 QR: /qr/admin?key=...
    @GetMapping("/admin")
    public String adminQrLogin(@RequestParam String key,
                               HttpServletRequest req,
                               HttpServletResponse res) {
        qrAuthService.loginAdminByKey(key, req, res);
        return "redirect:/admin/orders/hall";
    }

    // 고객 QR: /qr/table/12?key=...
    @GetMapping("/table/{tableNum}")
    public String tableQrLogin(@PathVariable Integer tableNum,
                               @RequestParam String key,
                               HttpServletRequest req,
                               HttpServletResponse res) {
        qrAuthService.loginTableByKey(tableNum, key, req, res);
        return "redirect:/items";
    }
}