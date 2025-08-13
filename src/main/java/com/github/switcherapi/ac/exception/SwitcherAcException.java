package com.github.switcherapi.ac.exception;

public class SwitcherAcException extends RuntimeException {

	public SwitcherAcException(final String url, final Exception e) {
		super(String.format("Something went wrong trying with url: %s", url), e);
	}

}
