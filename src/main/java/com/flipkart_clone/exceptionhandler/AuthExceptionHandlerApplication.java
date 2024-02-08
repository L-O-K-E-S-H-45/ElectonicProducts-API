package com.flipkart_clone.exceptionhandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.flipkart_clone.exception.IllegalRequestException;
import com.flipkart_clone.exception.InvalidOtpException;
import com.flipkart_clone.exception.OtpExpiredException;
import com.flipkart_clone.exception.UserAlreadyExistException;
import com.flipkart_clone.exception.UserExpiredException;
import com.flipkart_clone.exception.UserNotFoundByEmailException;

@RestControllerAdvice
public class AuthExceptionHandlerApplication extends ResponseEntityExceptionHandler {
	
	private ResponseEntity<Object> structure(HttpStatus status, String message, Object rootCause){
		return new ResponseEntity<Object>(Map.of(
				"message",message,
				"rootCause",rootCause,
				"status",status.value()), status);
	}
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<ObjectError> allErrors = ex.getAllErrors();
		Map<String, String> errors = new HashMap<>();
		allErrors.forEach(error->{
			errors.put(((FieldError)error).getField(), ((FieldError)error).getDefaultMessage());
		});
		return structure(HttpStatus.BAD_REQUEST,"Failed to save data", errors);
	}
	
	@ExceptionHandler(UserNotFoundByEmailException.class)
	public ResponseEntity<Object> handleUserNotFoundByEmailException(UserNotFoundByEmailException ex){
		return structure(HttpStatus.NOT_FOUND, ex.getMessage(), "User not found for requested Email!!!");
	}
	
	@ExceptionHandler(IllegalRequestException.class)
	public ResponseEntity<Object> handleIllegalRequestException(IllegalRequestException ex){
		return structure(HttpStatus.NOT_ACCEPTABLE, ex.getMessage(), "Illegal Input!!!");
	}
	
	@ExceptionHandler(InvalidOtpException.class)
	public ResponseEntity<Object> handleInvalidOtpException(InvalidOtpException ex){
		return structure(HttpStatus.NOT_ACCEPTABLE, ex.getMessage(), "Invalid OTP!!!");
	}
	
	@ExceptionHandler(OtpExpiredException.class)
	public ResponseEntity<Object> handleOtpExpiredException(OtpExpiredException ex){
		return structure(HttpStatus.NOT_FOUND, ex.getMessage(), "OTP Expired!!!");
	}
	
	@ExceptionHandler(UserAlreadyExistException.class)
	public ResponseEntity<Object> handleUserAlreadyExistException(UserAlreadyExistException ex){
		return structure(HttpStatus.NOT_ACCEPTABLE, ex.getMessage(), "User Already exist!!!");
	}
	
	@ExceptionHandler(UserExpiredException.class)
	public ResponseEntity<Object> handleUserExpiredException(UserExpiredException ex){
		return structure(HttpStatus.NOT_ACCEPTABLE, ex.getMessage(), "User session expired!!!");
	}

}
