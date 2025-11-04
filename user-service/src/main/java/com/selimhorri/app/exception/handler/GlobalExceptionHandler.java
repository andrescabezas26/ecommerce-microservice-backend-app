package com.selimhorri.app.exception.handler;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

import com.selimhorri.app.exception.wrapper.UsernameAlreadyExistsException;
import com.selimhorri.app.exception.wrapper.VerificationTokenNotFoundException;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.exception.wrapper.AddressNotFoundException;
import com.selimhorri.app.exception.wrapper.CredentialNotFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<?> handleAddressNotFound(AddressNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(VerificationTokenNotFoundException.class)
    public ResponseEntity<?> handleVerificationTokenNotFound(VerificationTokenNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<?> handleEmptyResultDataAccess(EmptyResultDataAccessException ex) {
        return buildResponse("No entity found to delete: " + ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFound(EntityNotFoundException ex) {
        return buildResponse("Entity not found: " + ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<?> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserObjectNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserObjectNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CredentialNotFoundException.class)
    public ResponseEntity<?> handleCredentialNotFound(CredentialNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(String message, HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toEpochMilli());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}
