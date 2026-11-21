package com.pe.pivot.yanki.domain.exception;

/**
 * Custom exception thrown when attempting to create a Yanki wallet that already exists.
 */
public class DuplicateWalletException extends RuntimeException {
  public DuplicateWalletException(String message) {
    super(message);
  }
}
