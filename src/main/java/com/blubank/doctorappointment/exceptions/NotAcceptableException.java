package com.blubank.doctorappointment.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class NotAcceptableException extends RuntimeException {

  public NotAcceptableException (String message) {
    super(String.format(message));
  }
}
