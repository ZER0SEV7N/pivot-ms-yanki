package com.pe.pivot.yanki;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Smoke test for MsYankiApplication class definition.
 */
class MsYankiApplicationTests {

  @Test
  @DisplayName("Should be instantiate MsYankiApplication successfully")
  void shouldBeInstantiateApplicationSuccessfully() {
    // Arrange & Act
    MsYankiApplication application = new MsYankiApplication();

    // Assert
    assertNotNull(application);
  }
}
