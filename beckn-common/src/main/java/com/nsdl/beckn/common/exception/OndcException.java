package com.nsdl.beckn.common.exception;

import static com.nsdl.beckn.common.constant.ApplicationConstant.PIPE;

public class OndcException extends RuntimeException {

	private static final long serialVersionUID = 4606666126597266141L;

	private final ErrorCodeOndc errorCode;

	public OndcException(final ErrorCodeOndc errorCode, final String message, final Throwable cause) {
		super(errorCode.getCode() + PIPE + message, cause);

		this.errorCode = errorCode;
	}

	public OndcException(final ErrorCodeOndc errorCode, final String message) {
		// super(errorCode.getCode() + PIPE + message, null);
		super(message, null);
		this.errorCode = errorCode;
	}

	public OndcException(final ErrorCodeOndc errorCode, final Throwable t) {
		// super(errorCode.getCode() + PIPE + message, null);
		super(errorCode.getMessage(), t);
		this.errorCode = errorCode;
	}

	public OndcException(final ErrorCodeOndc errorCode) {
		super(String.valueOf(errorCode.getCode()), new Throwable(errorCode.getMessage()));
		this.errorCode = errorCode;
	}

	public OndcException(int statusCode, String message) {
		super(statusCode + PIPE + message, null);
		this.errorCode = null;
	}

	public OndcException(final Throwable cause) {
		super(cause.getMessage(), cause);
		this.errorCode = null;
	}

	public ErrorCodeOndc getErrorCode() { return this.errorCode; }
}