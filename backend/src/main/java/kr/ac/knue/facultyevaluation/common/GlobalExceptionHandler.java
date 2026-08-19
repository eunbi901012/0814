package kr.ac.knue.facultyevaluation.common;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import kr.ac.knue.facultyevaluation.auth.AuthenticationRequiredException;
import kr.ac.knue.facultyevaluation.auth.InvalidCredentialsException;
import kr.ac.knue.facultyevaluation.auth.PermissionDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationRequiredException.class)
    public ResponseEntity<ApiError> authentication(AuthenticationRequiredException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiError.of("UNAUTHORIZED", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> invalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiError.of("INVALID_CREDENTIALS", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<ApiError> permissionDenied(PermissionDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiError.of("FORBIDDEN", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(this::toFieldError)
            .toList();
        return ResponseEntity.badRequest()
            .body(ApiError.of("VALIDATION_ERROR", "입력값을 확인해 주세요.", request.getRequestURI(), fieldErrors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> badRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest()
            .body(ApiError.of("BAD_REQUEST", ex.getMessage(), request.getRequestURI()));
    }

    private FieldErrorDetail toFieldError(FieldError fieldError) {
        return new FieldErrorDetail(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
