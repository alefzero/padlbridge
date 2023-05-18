package com.alefzero.padlbridge.exceptions;

/**
 * Indicates an recoverable situation.
 * 
 * @author xandecelo
 *
 */
public class PadlRecoverableException extends RuntimeException {

	private static final long serialVersionUID = -4061443096294545997L;

	public PadlRecoverableException() {
		super();
	}

	public PadlRecoverableException(String message) {
		super(message);
	}

	public PadlRecoverableException(String message, Throwable cause) {
		super(message, cause);
	}

	public PadlRecoverableException(Throwable cause) {
		super(cause);
	}
}
