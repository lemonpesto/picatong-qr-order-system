package lemon.qrordersystem.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(LocalDateTime.now(ZoneId.of("Asia/Seoul")), status, error, message, path);
    }
}
