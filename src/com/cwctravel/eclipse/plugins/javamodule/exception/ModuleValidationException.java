package com.cwctravel.eclipse.plugins.javamodule.exception;

public class ModuleValidationException extends Exception {
	private static final long serialVersionUID = -2243674291374176091L;
	private final String moduleName;

	public ModuleValidationException(String moduleName, String message, Throwable cause) {
		super(message, cause);
		this.moduleName = moduleName;
	}

	public ModuleValidationException(String moduleName, String message) {
		super(message);
		this.moduleName = moduleName;
	}

	public String getModuleName() {
		return moduleName;
	}
}
