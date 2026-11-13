package lk.bci.ordermanagementapi.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import lk.bci.ordermanagementapi.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("Validation failed on {}: {}", request.getRequestURI(), validationErrors);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed for one or more fields",
                request.getRequestURI(),
                validationErrors
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(
            Exception ex,
            HttpServletRequest request
    ) {
        String message = ex instanceof IllegalArgumentException
                ? ex.getMessage()
                : "Malformed request body or invalid parameter type";

        log.warn("Bad request on {}: {}", request.getRequestURI(), message);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler({
            DuplicateResourceException.class,
            InsufficientStockException.class,
            OrderStateConflictException.class,
            ObjectOptimisticLockingFailureException.class,
            OptimisticLockException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<ErrorResponse> handleConflictExceptions(
            Exception ex,
            HttpServletRequest request
    ) {
        String message;
        if (ex instanceof DuplicateResourceException
                || ex instanceof InsufficientStockException
                || ex instanceof OrderStateConflictException) {
            message = ex.getMessage();
        } else if (ex instanceof ObjectOptimisticLockingFailureException || ex instanceof OptimisticLockException) {
            message = "Concurrent modification conflict detected. Please retry.";
        } else {
            message = "Resource conflict or duplicate constraint violation";
        }

        log.warn("Conflict (409) on {}: {}", request.getRequestURI(), message);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Resource not found (404) on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception on {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected internal error occurred",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
