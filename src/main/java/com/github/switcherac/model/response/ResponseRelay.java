package com.github.switcherac.model.response;

public class ResponseRelay {
	
	private boolean result;
	
	private String message;
	
	public ResponseRelay(boolean result, String message) {
		this.result = result;
		this.message = message;
	}
	
	public ResponseRelay(boolean result) {
		this(result, null);
	}
	
	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
