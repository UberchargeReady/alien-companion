package com.gDyejeekis.aliencompanion.api.exception;

public class RedditError extends RuntimeException {

	private static final long serialVersionUID = -1039803118047533936L;

	public RedditError(String message) {
		super(message);
	}
	
}
