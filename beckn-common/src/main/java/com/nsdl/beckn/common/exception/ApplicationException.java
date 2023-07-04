package com.nsdl.beckn.common.exception;

import static com.nsdl.beckn.common.constant.ApplicationConstant.PIPE;

public class ApplicationException extends RuntimeException {

	private static final long serialVersionUID = 4606666126597266141L;

	private final ErrorCode errorCode;

	public ApplicationException(final ErrorCode errorCode, final String message, final Throwable cause) {
		super(errorCode.getCode() + PIPE + message, cause);

		this.errorCode = errorCode;
	}

	public ApplicationException(final ErrorCode errorCode, final String message) {
		super(errorCode.getCode() + PIPE + message, null);
		this.errorCode = errorCode;
	}

	public ApplicationException(final ErrorCode errorCode) {
		super(String.valueOf(errorCode.getCode()), new Throwable(errorCode.getMessage()));
		this.errorCode = errorCode;
	}

	public ApplicationException(int statusCode, String message) {
		super(statusCode + PIPE + message, null);
		this.errorCode = null;
	}

	public ApplicationException(final Throwable cause) {
		super(cause.getMessage(), cause);
		this.errorCode = null;
	}

	public ErrorCode getErrorCode() { return this.errorCode; }
}