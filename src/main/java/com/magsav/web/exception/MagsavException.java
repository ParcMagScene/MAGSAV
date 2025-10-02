package com.magsav.web.exception;

import org.springframework.http.HttpStatus;

/** Exception de base pour l'application MAGSAV */
public class MagsavException extends Exception {

  private final HttpStatus httpStatus;
  private final String userMessage;
  private final String technicalMessage;

  public MagsavException(HttpStatus httpStatus, String userMessage) {
    this(httpStatus, userMessage, userMessage);
  }

  public MagsavException(HttpStatus httpStatus, String userMessage, String technicalMessage) {
    super(technicalMessage);
    this.httpStatus = httpStatus;
    this.userMessage = userMessage;
    this.technicalMessage = technicalMessage;
  }

  public MagsavException(HttpStatus httpStatus, String userMessage, Throwable cause) {
    super(userMessage, cause);
    this.httpStatus = httpStatus;
    this.userMessage = userMessage;
    this.technicalMessage = userMessage + " - " + cause.getMessage();
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  public String getUserMessage() {
    return userMessage;
  }

  public String getTechnicalMessage() {
    return technicalMessage;
  }
}
