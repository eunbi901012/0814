package kr.ac.knue.facultyevaluation.common;

import java.time.Instant;
import java.util.List;

public record ApiError(boolean success, ErrorBody error) {

    public static ApiError of(String code, String message, String path) {
        return of(code, message, path, List.of());
    }

    public static ApiError of(String code, String message, String path, List<FieldErrorDetail> fieldErrors) {
        return new ApiError(false, new ErrorBody(code, message, fieldErrors, path, Instant.now()));
    }

    public record ErrorBody(String code, String message, List<FieldErrorDetail> fieldErrors, String path, Instant timestamp) {
    }
}
