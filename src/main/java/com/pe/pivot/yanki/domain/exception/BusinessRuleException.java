package com.pe.pivot.yanki.domain.exception;

/**
 * Custom exception thrown when a business rule is violated in the Yanki domain.
 */
public class BusinessRuleException extends RuntimeException {
  public BusinessRuleException(String message) {
    super(message);
  }
}
