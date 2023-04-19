package com.alefzero.padlbridge.exceptions;

/**
 * Indicates an unrecoverable situation. No class should treat or log or output
 * any message. This error can be catch and logged by the Orchestrator, but
 * still will exit.
 * 
 * @author xandecelo
 *
 */
public class PadlUnrecoverableError extends Error {

	private static final long serialVersionUID = -8169155289909088853L;

	public PadlUnrecoverableError() {
		super();
	}

	public PadlUnrecoverableError(String message) {
		super(message);
	}

	public PadlUnrecoverableError(String message, Throwable cause) {
		super(message, cause);
	}

	public PadlUnrecoverableError(Throwable cause) {
		super(cause);
	}
}
