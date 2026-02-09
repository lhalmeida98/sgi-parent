package ec.sgi.backend.interfaces;

import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.exception.EmailSendException;
import ec.sgi.backend.application.exception.ForbiddenException;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.exception.SriCoreException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
    return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(BusinessRuleException.class)
  public ResponseEntity<ApiError> handleBusiness(BusinessRuleException ex, HttpServletRequest request) {
    return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(SriCoreException.class)
  public ResponseEntity<ApiError> handleSri(SriCoreException ex, HttpServletRequest request) {
    return buildError(HttpStatus.BAD_GATEWAY, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
    return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(EmailSendException.class)
  public ResponseEntity<ApiError> handleEmail(EmailSendException ex, HttpServletRequest request) {
    return buildError(HttpStatus.BAD_GATEWAY, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
    String message = fieldError == null ? "Validacion invalida" : fieldError.getField() + ": " + fieldError.getDefaultMessage();
    return buildError(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
    return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado", request.getRequestURI());
  }

  private ResponseEntity<ApiError> buildError(HttpStatus status, String message, String path) {
    ApiError error = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, path);
    return ResponseEntity.status(status).body(error);
  }
}
