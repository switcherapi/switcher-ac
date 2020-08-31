package com.github.switcherac.service.exception;

public class ServiceException extends Exception {
	
	private static final long serialVersionUID = -2323087720138350880L;

	public ServiceException(String message, Throwable e) {
		super(message, e);
	}

}
