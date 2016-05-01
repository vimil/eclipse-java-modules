package com.cwctravel.eclipse.plugins.javamodule.exception;

public class PackagePatternValidationException extends Exception {
	private static final long serialVersionUID = 6387041184039599623L;

	public PackagePatternValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public PackagePatternValidationException(String message) {
		super(message);
	}

}
