package lemon.qrordersystem.dto;

import lemon.qrordersystem.exception.ErrorCode;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String code,
        String message,
        String path
) {
    public static ApiErrorResponse of(ErrorCode errorCode, String message, String path) {
        return new ApiErrorResponse(
                LocalDateTime.now(ZoneId.of("Asia/Seoul")),
                errorCode.getStatus().value(),
                errorCode.getStatus().getReasonPhrase(),
                errorCode.name(),
                message,
                path
        );
    }
}