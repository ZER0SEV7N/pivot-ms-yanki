package com.pe.pivot.yanki.infrastructure.adapter.in.web.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pe.pivot.yanki.domain.exception.BusinessRuleException;
import com.pe.pivot.yanki.domain.exception.DuplicateWalletException;
import com.pe.pivot.yanki.domain.exception.PaymentNotFoundException;
import com.pe.pivot.yanki.domain.exception.WalletNotFoundException;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.ErrorResponse;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

/**
 * Unit test suite for the Yanki GlobalExceptionHandler.
 */
class GlobalExceptionHandlerTest {

  private static final String PATH = "/api/v1/yanki/wallets/999111222";

  private GlobalExceptionHandler handler;
  private ServerWebExchange exchange;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
    exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get(PATH).build());
  }

  @Test
  @DisplayName("Should be return 404 when a wallet is not found (happy path)")
  void shouldBeReturnNotFoundForWallet() {
    // Arrange
    WalletNotFoundException exception =
            new WalletNotFoundException("Yanki wallet not found");

    // Act
    Single<ResponseEntity<ErrorResponse>> result =
            handler.handleNotFound(exception, exchange);

    // Assert
    assertErrorResponse(result, HttpStatus.NOT_FOUND, "Yanki wallet not found");
  }

  @Test
  @DisplayName("Should be return 404 when a payment is not found")
  void shouldBeReturnNotFoundForPayment() {
    // Arrange
    PaymentNotFoundException exception =
            new PaymentNotFoundException("Yanki payment not found");

    // Act
    Single<ResponseEntity<ErrorResponse>> result =
            handler.handleNotFound(exception, exchange);

    // Assert
    assertErrorResponse(result, HttpStatus.NOT_FOUND, "Yanki payment not found");
  }

  @Test
  @DisplayName("Should be return 409 when the wallet is duplicated")
  void shouldBeReturnConflict() {
    // Arrange
    DuplicateWalletException exception =
            new DuplicateWalletException("Duplicate document number");

    // Act
    Single<ResponseEntity<ErrorResponse>> result =
            handler.handleDuplicate(exception, exchange);

    // Assert
    assertErrorResponse(result, HttpStatus.CONFLICT, "Duplicate document number");
  }

  @Test
  @DisplayName("Should be return 400 when a business rule is violated")
  void shouldBeReturnBadRequestForBusinessRule() {
    // Arrange
    BusinessRuleException exception =
            new BusinessRuleException("Insufficient balance");

    // Act
    Single<ResponseEntity<ErrorResponse>> result =
            handler.handleBusinessRule(exception, exchange);

    // Assert
    assertErrorResponse(result, HttpStatus.BAD_REQUEST, "Insufficient balance");
  }

  @Test
  @DisplayName("Should be return 400 with joined messages when validation fails")
  void shouldBeReturnBadRequestForValidationErrors() {
    // Arrange
    BeanPropertyBindingResult bindingResult =
            new BeanPropertyBindingResult(new Object(), "yankiWalletRequest");
    bindingResult.addError(
            new ObjectError("yankiWalletRequest", "phoneNumber is required"));
    bindingResult.addError(
            new ObjectError("yankiWalletRequest", "email must have a valid format"));
    WebExchangeBindException exception = new WebExchangeBindException(null, bindingResult);

    // Act
    Single<ResponseEntity<ErrorResponse>> result =
            handler.handleValidationErrors(exception, exchange);

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(response -> {
              assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
              assertNotNull(response.getBody());
              assertTrue(response.getBody().message().contains("phoneNumber is required"));
              assertTrue(response.getBody().message()
                      .contains("email must have a valid format"));
              assertEquals(PATH, response.getBody().path());
              return true;
            })
            .assertComplete();
  }

  @Test
  @DisplayName("Should be return 500 when an unexpected error occurs")
  void shouldBeReturnInternalServerError() {
    // Arrange
    Exception exception = new RuntimeException("Unexpected failure");

    // Act
    Single<ResponseEntity<ErrorResponse>> result =
            handler.handleGenericException(exception, exchange);

    // Assert
    assertErrorResponse(result, HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected internal server error occurred.");
  }

  private void assertErrorResponse(Single<ResponseEntity<ErrorResponse>> result,
                                   HttpStatus expectedStatus, String expectedMessage) {
    result.test()
            .assertNoErrors()
            .assertValue(response -> {
              assertEquals(expectedStatus, response.getStatusCode());
              assertNotNull(response.getBody());
              assertEquals(expectedStatus.value(), response.getBody().status());
              assertEquals(expectedStatus.getReasonPhrase(), response.getBody().error());
              assertEquals(expectedMessage, response.getBody().message());
              assertEquals(PATH, response.getBody().path());
              assertNotNull(response.getBody().timestamp());
              return true;
            })
            .assertComplete();
  }
}
