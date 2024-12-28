package com.fordpro.cloudrun.exception;

import com.fordpro.cloudrun.models.FinMappingException;

import com.fordpro.cloudrun.models.StandardErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = FinMappingException.class)
    public ResponseEntity<StandardErrorResponse> handleFinMappingException(FinMappingException finMappingException, HttpServletRequest httpServletRequest) {
        StandardErrorResponse standardErrorResponse = StandardErrorResponse.builder().message(finMappingException.getMessage()).requestedUri(httpServletRequest.getRequestURI()).build();
        return new ResponseEntity<>(standardErrorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<StandardErrorResponse> handleMissingParamsException(Exception exception, HttpServletRequest httpServletRequest) {
        StandardErrorResponse standardErrorResponse = StandardErrorResponse.builder().message(exception.getMessage()).requestedUri(httpServletRequest.getRequestURI()).build();
        return new ResponseEntity<>(standardErrorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({BindException.class, HttpMessageNotReadableException.class, ConstraintViolationException.class})
    public ResponseEntity<StandardErrorResponse> handleBadRequests(Exception exception, HttpServletRequest httpServletRequest) {
        String errorMessage = (exception instanceof BindException bindException) ?
                bindException.getBindingResult().getAllErrors().get(0).getDefaultMessage() :
                exception.getMessage();
        StandardErrorResponse standardErrorResponse = StandardErrorResponse.builder().message(errorMessage).requestedUri(httpServletRequest.getRequestURI()).build();
        return new ResponseEntity<>(standardErrorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = AuthenticationException.class)
    public ResponseEntity<StandardErrorResponse> handleUnauthorizedException(AuthenticationException authenticationException, HttpServletRequest httpServletRequest) {
        StandardErrorResponse standardErrorResponse = StandardErrorResponse.builder().message(authenticationException.getMessage()).requestedUri(httpServletRequest.getRequestURI()).build();
        return new ResponseEntity<>(standardErrorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<StandardErrorResponse> handleForbiddenException(AccessDeniedException accessDeniedException, HttpServletRequest httpServletRequest) {
        StandardErrorResponse standardErrorResponse = StandardErrorResponse.builder().message(accessDeniedException.getMessage()).requestedUri(httpServletRequest.getRequestURI()).build();
        return new ResponseEntity<>(standardErrorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({HttpClientErrorException.class, ResponseStatusException.class})
    public ResponseEntity<StandardErrorResponse> handleResponseStatusException(ResponseStatusException responseStatusException, HttpServletRequest httpServletRequest) {
        StandardErrorResponse standardErrorResponse = StandardErrorResponse.builder().message(responseStatusException.getMessage()).requestedUri(httpServletRequest.getRequestURI()).build();
        if (responseStatusException.getStatusCode() == HttpStatus.NOT_FOUND) {
            return new ResponseEntity<>(standardErrorResponse, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(standardErrorResponse, responseStatusException.getStatusCode());
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<StandardErrorResponse> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException httpRequestMethodNotSupportedException, HttpServletRequest httpServletRequest) {
        StandardErrorResponse standardErrorResponse = StandardErrorResponse.builder().message(httpRequestMethodNotSupportedException.getMessage()).requestedUri(httpServletRequest.getRequestURI()).build();
        return new ResponseEntity<>(standardErrorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<StandardErrorResponse> handleHttpMediaTypeException(Exception exception, HttpServletRequest httpServletRequest) {
        StandardErrorResponse standardErrorResponse = StandardErrorResponse.builder().message(exception.getMessage()).requestedUri(httpServletRequest.getRequestURI()).build();
        return new ResponseEntity<>(standardErrorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(value = HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<StandardErrorResponse> handleTooManyRequestsException(Exception exception, HttpServletRequest httpServletRequest) {
        StandardErrorResponse standardErrorResponse = StandardErrorResponse.builder().message(exception.getMessage()).requestedUri(httpServletRequest.getRequestURI()).build();
        return new ResponseEntity<>(standardErrorResponse, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<StandardErrorResponse> handleException(Exception exception, HttpServletRequest httpServletRequest) {
        StandardErrorResponse standardErrorResponse = StandardErrorResponse.builder().message(exception.getMessage() + " : " + exception.getClass()).requestedUri(httpServletRequest.getRequestURI()).build();
        return new ResponseEntity<>(standardErrorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

 