package com.soa.rs.discordbot.v3.cfg;

import com.soa.rs.discordbot.v3.jaxb.AdminEvent;
import com.soa.rs.discordbot.v3.jaxb.DiscordConfiguration;
import com.soa.rs.discordbot.v3.jaxb.ListingEvent;
import com.soa.rs.discordbot.v3.jaxb.MusicPlayer;
import com.soa.rs.discordbot.v3.jaxb.UserTrackingEvent;

/**
 * A ConfigValidator is used to validate that a bot's configuration is valid.
 * All events should be validated individually to ensure that they are valid.
 */
public interface ConfigValidator {

	/**
	 * Validates a Listing Event configuration
	 * 
	 * @param event
	 *            The event containing the parameters to validate
	 * @return True if the event passes validation, false otherwise
	 * @throws InvalidBotConfigurationException
	 *             Thrown if the event does cannot be successfully validated
	 */
	public boolean validateListingEvent(ListingEvent event) throws InvalidBotConfigurationException;

	/**
	 * Validates a Music Player Event configuration
	 * 
	 * @param event
	 *            The event containing the parameters to validate
	 * @return True if the event passes validation, false otherwise
	 * @throws InvalidBotConfigurationException
	 *             Thrown if the event does cannot be successfully validated
	 */
	public boolean validateMusicPlayer(MusicPlayer event) throws InvalidBotConfigurationException;

	/**
	 * Validates an Admin Event configuration
	 * 
	 * @param event
	 *            The event containing the parameters to validate
	 * @return True if the event passes validation, false otherwise
	 * @throws InvalidBotConfigurationException
	 *             Thrown if the event does cannot be successfully validated
	 */
	public boolean validateAdminEvent(AdminEvent event) throws InvalidBotConfigurationException;

	/**
	 * Validates an User Tracking Event configuration
	 * 
	 * @param event
	 *            The event containing the parameters to validate
	 * @return True if the event passes validation, false otherwise
	 * @throws InvalidBotConfigurationException
	 *             Thrown if the event does cannot be successfully validated
	 */
	public boolean validateUserTrackingEvent(UserTrackingEvent event) throws InvalidBotConfigurationException;

	/**
	 * Validates that the current configuration conforms to the XML Schema. It is
	 * assumed that a configuration that conforms to this schema is valid for
	 * runtime.
	 * 
	 * @param cfg
	 *            The configuration to validate
	 * @return True if the configuration passes validation, false otherwise
	 */
	public boolean validateConformsToSchema(DiscordConfiguration cfg);

}
