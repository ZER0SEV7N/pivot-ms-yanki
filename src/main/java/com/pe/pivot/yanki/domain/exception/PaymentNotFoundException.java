package com.pe.pivot.yanki.domain.exception;

/**
 * Custom exception thrown when a requested Yanki payment is not found in the system.
 */
public class PaymentNotFoundException extends RuntimeException {
  public PaymentNotFoundException(String message) {
    super(message);
  }
}
