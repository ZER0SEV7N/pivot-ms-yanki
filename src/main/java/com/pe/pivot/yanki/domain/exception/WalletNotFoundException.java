package com.pe.pivot.yanki.domain.exception;

/**
 * Custom exception thrown when a requested Yanki wallet is not found in the system.
 */
public class WalletNotFoundException extends RuntimeException {
  public WalletNotFoundException(String message) {
    super(message);
  }
}
