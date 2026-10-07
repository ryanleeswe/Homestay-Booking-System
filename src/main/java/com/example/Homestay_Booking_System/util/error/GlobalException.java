package com.example.Homestay_Booking_System.util.error;

import java.util.stream.Collectors;
import com.example.Homestay_Booking_System.domain.dto.RestResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalException {
    private static final Logger log = LoggerFactory.getLogger(GlobalException.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Void>> handleException(Exception exception) {
        if (exception instanceof ErrorResponse response) {
            int status = response.getStatusCode().value();
            HttpStatus httpStatus = HttpStatus.resolve(status);
            String error = httpStatus == null ? "HTTP Error" : httpStatus.getReasonPhrase();
            String message = response.getBody().getDetail();
            return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders())
                    .body(new RestResponse<>(status, message == null ? error : message, error, null));
        }
        log.error("Unhandled request exception", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống");
    }

    @ExceptionHandler(IdInvalidException.class)
    public ResponseEntity<RestResponse<Void>> handleIdInvalidException(IdInvalidException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RestResponse<Void>> handleNoResourceFoundException(NoResourceFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "Không tìm thấy tài nguyên yêu cầu");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Void>> validationError(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getAllErrors().stream()
                .map(item -> (item instanceof FieldError field ? field.getField() + ": " : "")
                        + item.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RestResponse<Void>> handleConstraintViolation(ConstraintViolationException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getConstraintViolations().stream()
                .map(item -> item.getPropertyPath() + ": " + item.getMessage())
                .sorted().collect(Collectors.joining("; ")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RestResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return error(HttpStatus.BAD_REQUEST, "Nội dung yêu cầu bị thiếu hoặc sai định dạng JSON");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RestResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return error(HttpStatus.BAD_REQUEST, "Tham số không hợp lệ: " + exception.getName());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RestResponse<Void>> handleDataIntegrity(DataIntegrityViolationException exception) {
        return error(HttpStatus.CONFLICT, "Dữ liệu bị trùng hoặc vi phạm ràng buộc");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<RestResponse<Void>> handleAuthenticationException(AuthenticationException exception) {
        return error(HttpStatus.UNAUTHORIZED, "Thông tin xác thực không hợp lệ hoặc đã hết hạn");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RestResponse<Void>> handleAccessDeniedException(AccessDeniedException exception) {
        return error(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này");
    }

    private ResponseEntity<RestResponse<Void>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new RestResponse<>(status.value(), message, status.getReasonPhrase(), null));
    }
}
