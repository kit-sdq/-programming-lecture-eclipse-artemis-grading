/* Licensed under EPL-2.0 2022. */
package edu.kit.kastel.eclipse.common.api;

public final class ArtemisClientException extends Exception {
	private static final long serialVersionUID = -1022345135975530727L;

	public ArtemisClientException(String message) {
		super(message);
	}

	public ArtemisClientException(String message, Throwable cause) {
		super(message, cause);
	}
}
