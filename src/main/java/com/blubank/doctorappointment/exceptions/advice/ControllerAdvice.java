package com.blubank.doctorappointment.exceptions.advice;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import com.blubank.doctorappointment.exceptions.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;


@RestControllerAdvice
public class ControllerAdvice {
  @ExceptionHandler(value = NotAcceptableException.class)
  @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
  public ErrorMessage handleNotAcceptableException(NotAcceptableException ex, WebRequest request) {
    return new ErrorMessage(
        HttpStatus.NOT_ACCEPTABLE.value(),
        new Date(),
        ex.getMessage(),
        request.getDescription(false));
  }

  @ExceptionHandler(value = NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorMessage handleNotFoundException(NotFoundException ex, WebRequest request) {
    return new ErrorMessage(
        HttpStatus.NOT_FOUND.value(),
        new Date(),
        ex.getMessage(),
        request.getDescription(false));
  }

  @ExceptionHandler(value = BadRequestException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorMessage handleBadRequestException(BadRequestException ex, WebRequest request) {
    return new ErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            new Date(),
            ex.getMessage(),
            request.getDescription(false));
  }
  @ExceptionHandler(value = ConflictException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ErrorMessage handleConflictException(ConflictException ex, WebRequest request) {
    return new ErrorMessage(
            HttpStatus.CONFLICT.value(),
            new Date(),
            ex.getMessage(),
            request.getDescription(false));
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ErrorMessage handleNotValidExceptions(MethodArgumentNotValidException ex, WebRequest request) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
    return new ErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            new Date(),
            errors.toString(),
            request.getDescription(false));
  }


  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ErrorMessage handleNotReadableException(HttpMessageNotReadableException ex, WebRequest  request) {
    return new ErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            new Date(),
            ex.getMessage(),
            request.getDescription(false));

  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(BindException.class)
  public ErrorMessage handleBindExceptions(BindException ex, WebRequest request) {
    return new ErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            new Date(),
            ex.getMessage(),
            request.getDescription(false));
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ErrorMessage handleMissingParameterExceptions(MissingServletRequestParameterException ex, WebRequest request) {
    return new ErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            new Date(),
            ex.getMessage(),
            request.getDescription(false));
  }

  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ErrorMessage handleMethodNotSupportExceptions(HttpRequestMethodNotSupportedException ex, WebRequest request) {
    return new ErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            new Date(),
            ex.getMessage(),
            request.getDescription(false));
  }
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MissingRequestHeaderException.class)
  public ErrorMessage handleMissingRequestHeaderExceptions(MissingRequestHeaderException ex, WebRequest request) {
    return new ErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            new Date(),
            ex.getMessage(),
            request.getDescription(false));
  }
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MissingServletRequestPartException.class)
  public ErrorMessage handleMissingServletRequestPartExceptions(MissingServletRequestPartException ex, WebRequest request) {
    return new ErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            new Date(),
            ex.getMessage(),
            request.getDescription(false));
  }
}