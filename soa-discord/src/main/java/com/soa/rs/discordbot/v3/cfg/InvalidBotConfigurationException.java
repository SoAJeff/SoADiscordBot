package com.soa.rs.discordbot.v3.cfg;

/**
 * An InvalidBotConfigurationException is thrown if the configuration is
 * determined to be invalid. For example, if the information does not conform to
 * the schema, or is missing a parameter in order to run.
 */
public class InvalidBotConfigurationException extends Exception {

	private static final long serialVersionUID = -2805066371428043276L;
	private EventTypes eventType;

	public InvalidBotConfigurationException() {
	}

	/**
	 * Throws the exception with the defined message
	 * 
	 * @param message
	 *            The message associated with the exception
	 */
	public InvalidBotConfigurationException(String message) {
		super(message);
	}

	/**
	 * Throws the exception with the defined message
	 * 
	 * @param cause
	 *            The cause of the exception
	 */
	public InvalidBotConfigurationException(Throwable cause) {
		super(cause);
	}

	/**
	 * Throws the exception with the defined message and cause
	 * 
	 * @param message
	 *            The message associated with the exception
	 * @param cause
	 *            The cause of the exception
	 */
	public InvalidBotConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidBotConfigurationException(String message, EventTypes eventType) {
		super(message);
		this.eventType = eventType;
	}

	public EventTypes getEventType() {
		return this.eventType;
	}

}
