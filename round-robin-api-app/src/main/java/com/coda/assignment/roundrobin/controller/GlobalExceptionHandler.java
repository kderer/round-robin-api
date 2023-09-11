package com.coda.assignment.roundrobin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coda.assignment.roundrobin.exception.NoAvailableInstanceException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(NoAvailableInstanceException.class)
	@ResponseBody
	public ResponseEntity<String> handleDuplicateTransactionIdException(HttpServletRequest req,
			NoAvailableInstanceException ex) {
		return new ResponseEntity<String>("No available server instance", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public ResponseEntity<String> handleException(HttpServletRequest req, Exception ex) {
		String message = ex.getMessage();

		if (!StringUtils.hasText(message)) {
			Throwable cause = ex.getCause();

			if (cause != null) {
				message = cause.getMessage();
			}

			if (!StringUtils.hasText(message)) {
				message = ex.getClass().getName();
			}
		}

		return new ResponseEntity<String>(message, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
