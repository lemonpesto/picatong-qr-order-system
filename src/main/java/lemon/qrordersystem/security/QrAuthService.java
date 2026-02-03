package lemon.qrordersystem.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lemon.qrordersystem.entity.table.TableEntity;
import lemon.qrordersystem.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QrAuthService {

    private final String adminKey;
    private final TableRepository tableRepository;
    private final SecurityContextRepository securityContextRepository;


    public QrAuthService(
            @Value("${app.qr.admin-key}") String adminKey,
            TableRepository tableRepository,
            SecurityContextRepository securityContextRepository
    ) {
        this.adminKey = adminKey;
        this.tableRepository = tableRepository;
        this.securityContextRepository = securityContextRepository;
    }

    public void loginAdminByKey(String key, HttpServletRequest req, HttpServletResponse res) {
        if (key == null || !key.equals(adminKey)) {
            throw new IllegalArgumentException("Invalid admin key");
        }

        CustomUserDetails admin = new CustomUserDetails(
                0L,
                "admin",
                "ADMIN",
                null
        );

        var auth = new UsernamePasswordAuthenticationToken(
                admin,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        saveAuthentication(auth, req, res);
    }

    public void loginTableByKey(Integer tableNum, String key, HttpServletRequest req, HttpServletResponse res) {
        TableEntity table = tableRepository
                .findByTableNumAndAccessKey(tableNum, key)
                .orElseThrow(() -> new IllegalArgumentException("Invalid table key"));

        CustomUserDetails user = new CustomUserDetails(
                table.getId(),
                "table-" + table.getTableNum(),
                "USER",
                table.getTableNum()
        );

        var auth = new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        saveAuthentication(auth, req, res);
    }

    private void saveAuthentication(UsernamePasswordAuthenticationToken auth,
                                    HttpServletRequest req,
                                    HttpServletResponse res) {

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // 세션에 SecurityContext 저장(= 로그인 상태 유지(이후 요청에서 @AuthenticationPrincipal 주입됨))
        securityContextRepository.saveContext(context, req, res);
    }
}
