package com.soa.rs.triviacreator.util;

import com.soa.rs.triviacreator.jaxb.TriviaConfiguration;
import com.soa.rs.triviacreator.jaxb.TriviaQuestion;

public class TriviaConfigValidator {
	
	/**
	 * Validate that the configuration provided has all fields appropriately
	 * validated so that Trivia can run successfully. Any configuration that has
	 * gotten to this point has already passed XML Schema validation, so this is
	 * checking to make sure the values entered make sense for use.
	 * 
	 * @param configuration
	 *            The Trivia Configuration uploaded to the Discord bot.
	 * @throws InvalidTriviaConfigurationException
	 *             If the configuration is for some reason not valid.
	 */
	public static void validateConfiguration(TriviaConfiguration configuration)
			throws InvalidTriviaConfigurationException {
		if (configuration.getTriviaName() == null || configuration.getTriviaName().isEmpty()) {
			throw new InvalidTriviaConfigurationException(
					"The server name field is required, and is empty in the provided configuration");
		}
		if (configuration.getServerId() == null || configuration.getServerId().isEmpty()) {
			throw new InvalidTriviaConfigurationException(
					"The server id field is required, and is empty in the provided configuration");
		}
		try {
			Long.parseLong(configuration.getServerId());
		} catch (NumberFormatException ex) {
			throw new InvalidTriviaConfigurationException(
					"The server id field is not valid (should be a long, but could not be parsed as a long)");
		}
		if (configuration.getChannelId() == null || configuration.getChannelId().isEmpty()) {
			throw new InvalidTriviaConfigurationException(
					"The channel id field is required, and is empty in the provided configuration");
		}
		try {
			Long.parseLong(configuration.getChannelId());
		} catch (NumberFormatException ex) {
			throw new InvalidTriviaConfigurationException(
					"The channel id field is not valid (should be a long, but could not be parsed as a long)");
		}
		if (configuration.getWaitTime() <= 0) {
			throw new InvalidTriviaConfigurationException("The wait time cannot be less than 1");
		}
		for (TriviaQuestion question : configuration.getQuestionBank().getTriviaQuestion()) {
			if (question.getQuestion() == null || question.getQuestion().isEmpty()) {
				throw new InvalidTriviaConfigurationException(
						"One of the Trivia Questions provided has no text in the question field.");
			}
			if (question.getAnswer() == null || question.getAnswer().isEmpty()) {
				throw new InvalidTriviaConfigurationException(
						"One of the Trivia Questions provided has no text in the answer field.");
			}
		}

	}

}
