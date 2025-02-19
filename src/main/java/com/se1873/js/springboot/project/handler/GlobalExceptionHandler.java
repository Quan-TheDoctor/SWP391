package com.se1873.js.springboot.project.handler;

import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class GlobalExceptionHandler {
//  @ExceptionHandler(ChangeSetPersister.NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFoundException(ChangeSetPersister.NotFoundException ex) {
    ErrorResponse er = new ErrorResponse() {
      @Override
      public HttpStatusCode getStatusCode() {
        return HttpStatusCode.valueOf(404);
      }

      @Override
      public ProblemDetail getBody() {
        return ProblemDetail.forStatusAndDetail(
          HttpStatusCode.valueOf(404),
          "Resource not found: " + ex.getMessage()
        );
      }
    };
    return ResponseEntity.status(404).body(er);
  }

//  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
    ErrorResponse er = new ErrorResponse() {
      @Override
      public HttpStatusCode getStatusCode() {
        return HttpStatusCode.valueOf(500);
      }

      @Override
      public ProblemDetail getBody() {
        return ProblemDetail.forStatusAndDetail(
          HttpStatusCode.valueOf(500),
          "Unknown error occurred: " + ex.getMessage()
        );
      }
    };
    return ResponseEntity.status(500).body(er);
  }
}