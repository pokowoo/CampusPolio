
package com.campuspolio.global.exception;

import com.campuspolio.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();

        log.warn("CustomException 발생: {}", errorCode.getMessage(), e);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(
                        false,
                        errorCode.name(),
                        errorCode.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult()
                .getAllErrors()
                .get(0)
                .getDefaultMessage();

        log.warn("Validation error: {}", message);

        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        false,
                        "VALIDATION_ERROR",
                        message
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {

        log.error("Unhandled Exception occurred", e);

        return ResponseEntity
                .internalServerError()
                .body(new ErrorResponse(
                        false,
                        ErrorCode.INTERNAL_SERVER_ERROR.name(),
                        ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
                ));
    }
}
