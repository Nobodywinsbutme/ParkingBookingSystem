package com.app.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ModelAndView handleApiException(ApiException ex, HttpServletResponse response) {
        response.setStatus(ex.getStatus().value());
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("message", ex.getMessage());
        return mv;
    }

    @ExceptionHandler(BookingConflictException.class)
    public ModelAndView handleBookingConflict(BookingConflictException ex, HttpServletResponse response) {
        response.setStatus(ex.getStatus().value());
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("message", ex.getMessage());
        return mv;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ModelAndView handleDataIntegrity(DataIntegrityViolationException ex, HttpServletResponse response) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        response.setStatus(HttpStatus.CONFLICT.value());
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("message", "This operation violates a database rule (e.g. duplicate slot code or duplicate payment for a booking).");
        return mv;
    }

    @ExceptionHandler(Exception.class)
    public Object handleUnknown(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        String ctx = request.getContextPath();
        String uri = request.getRequestURI();
        if (uri.startsWith(ctx + "/api/") || uri.equals(ctx + "/payment/webhook")) {
            log.error("Unhandled API/webhook error", ex);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
        log.error("Unhandled error", ex);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("message", "Internal server error");
        return mv;
    }
}
