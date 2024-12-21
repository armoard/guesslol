package com.guesslol.exceptions;

public class RoundAlreadyStartedException extends RuntimeException {
  public RoundAlreadyStartedException(String message) {
    super(message);
  }
}
