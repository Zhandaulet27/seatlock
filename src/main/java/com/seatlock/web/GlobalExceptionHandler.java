package com.seatlock.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Every ResponseStatusException thrown anywhere in the app (BookingService's
 * 404/409/403s, EventService's, etc.) used to be resolved by Spring MVC via
 * response.sendError(...), which triggers a servlet-container forward to
 * /error. That forward is a brand new request as far as Spring Security's
 * authorizeHttpRequests rules are concerned, and it was getting rejected
 * before BasicErrorController could ever render a body - the client just saw
 * an empty 403/404 with none of the actual reason text.
 * <p>
 * Handling ResponseStatusException here instead means Spring MVC builds the
 * response directly, with no sendError/forward round trip - so the reason
 * message set at the throw site always reaches the client, and the status
 * code is never re-evaluated by the security filter chain.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setDetail(ex.getReason());
        return problem;
    }
}
