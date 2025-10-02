package com.magsav;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HelloWorldTest {
  @Test
  void greet_returnsHelloWorld() {
    assertEquals("Hello, World!", HelloWorld.greet());
  }
}
