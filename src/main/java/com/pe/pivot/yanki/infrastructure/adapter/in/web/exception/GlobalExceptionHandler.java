package com.pe.pivot.yanki.infrastructure.adapter.in.web.exception;

import com.pe.pivot.yanki.domain.exception.BusinessRuleException;
import com.pe.pivot.yanki.domain.exception.DuplicateWalletException;
import com.pe.pivot.yanki.domain.exception.PaymentNotFoundException;
import com.pe.pivot.yanki.domain.exception.WalletNotFoundException;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.ErrorResponse;
import io.reactivex.rxjava3.core.Single;
import java.time.Instant;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

/**
 * Global Exception Handler translating Yanki domain exceptions into ErrorResponse.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles exceptions for not found resources, such as WalletNotFoundException.
   *
   * @param ex - the exception instance
   * @param exchange - the ServerWebExchange providing request context
   * @return - a Single emitting a ResponseEntity with status 404 (Not Found)
   */
  @ExceptionHandler({WalletNotFoundException.class, PaymentNotFoundException.class})
  public Single<ResponseEntity<ErrorResponse>> handleNotFound(
          RuntimeException ex, ServerWebExchange exchange) {
    log.warn("Resource not found: {}", ex.getMessage());
    return Single.just(buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), exchange));
  }

  /**
   * Handles duplicate wallet conflicts, such as DuplicateWalletException.
   *
   * @param ex - the exception instance
   * @param exchange - the ServerWebExchange providing request context
   * @return - a Single emitting a ResponseEntity with status 409 (Conflict)
   */
  @ExceptionHandler(DuplicateWalletException.class)
  public Single<ResponseEntity<ErrorResponse>> handleDuplicate(
          DuplicateWalletException ex, ServerWebExchange exchange) {
    log.warn("Duplicate Yanki wallet conflict: {}", ex.getMessage());
    return Single.just(buildResponse(HttpStatus.CONFLICT, ex.getMessage(), exchange));
  }

  /**
   * Handles exceptions for business rule violations, such as BusinessRuleException.
   *
   * @param ex - the exception instance
   * @param exchange - the ServerWebExchange providing request context
   * @return - a Single emitting a ResponseEntity with status 400 (Bad Request)
   */
  @ExceptionHandler(BusinessRuleException.class)
  public Single<ResponseEntity<ErrorResponse>> handleBusinessRule(
          BusinessRuleException ex, ServerWebExchange exchange) {
    log.warn("Business rule violation: {}", ex.getMessage());
    return Single.just(buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange));
  }

  /**
   * Handles validation errors from request payloads, such as WebExchangeBindException.
   *
   * @param ex - the exception instance containing validation errors
   * @param exchange - the ServerWebExchange providing request context
   * @return - a Single emitting a ResponseEntity with status 400 (Bad Request)
   */
  @ExceptionHandler(WebExchangeBindException.class)
  public Single<ResponseEntity<ErrorResponse>> handleValidationErrors(
          WebExchangeBindException ex, ServerWebExchange exchange) {
    String errors = ex.getBindingResult().getAllErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining("; "));
    log.warn("Payload validation error: {}", errors);
    return Single.just(buildResponse(HttpStatus.BAD_REQUEST, errors, exchange));
  }

  /**
   * Handles any unexpected exceptions that are not explicitly handled by other methods.
   *
   * @param ex - the exception instance
   * @param exchange - the ServerWebExchange providing request context
   * @return - a Single emitting a ResponseEntity with status 500 (Internal Server Error)
   */
  @ExceptionHandler(Exception.class)
  public Single<ResponseEntity<ErrorResponse>> handleGenericException(
          Exception ex, ServerWebExchange exchange) {
    log.error("Unexpected error in ms-yanki: ", ex);
    return Single.just(buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected internal server error occurred.", exchange));
  }

  /**
   * Builds a standardized ErrorResponse and wraps it in a ResponseEntity.
   *
   * @param status - the HTTP status to set in the response
   * @param message - the error message to include in the response
   * @param exchange - the ServerWebExchange providing request context
   * @return - a ResponseEntity containing the ErrorResponse and the specified HTTP status
   */
  private ResponseEntity<ErrorResponse> buildResponse(
          HttpStatus status, String message, ServerWebExchange exchange) {
    ErrorResponse error = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .path(exchange.getRequest().getPath().value())
            .build();
    return ResponseEntity.status(status).body(error);
  }
}