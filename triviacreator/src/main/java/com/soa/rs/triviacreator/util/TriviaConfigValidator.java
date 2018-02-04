package com.soa.rs.triviacreator.util;

import java.util.List;

import com.soa.rs.triviacreator.jaxb.Mode;
import com.soa.rs.triviacreator.jaxb.TriviaConfiguration;
import com.soa.rs.triviacreator.jaxb.TriviaQuestion;

public class TriviaConfigValidator {

	/**
	 * Validate that the configuration provided has all fields appropriately
	 * validated so that Trivia can run successfully. Any configuration that is
	 * using this entrypoint has already passed XML Schema validation, so this is
	 * checking to make sure the values entered make sense for use.
	 * 
	 * @param configuration
	 *            The Trivia Configuration uploaded to the Discord bot.
	 * @throws InvalidTriviaConfigurationException
	 *             If the configuration is for some reason not valid.
	 */
	public static void validateConfiguration(TriviaConfiguration configuration)
			throws InvalidTriviaConfigurationException {
		validateTriviaName(configuration.getTriviaName());
		validateServerOrChannelId(configuration.getServerId(), "server");
		validateServerOrChannelId(configuration.getChannelId(), "channel");
		validateWaitTime(configuration.getWaitTime(), configuration.getMode());
		validateQuestions(configuration.getQuestionBank().getTriviaQuestion());

	}

	public static void validateTriviaName(String name) throws InvalidTriviaConfigurationException {
		if (name == null || name.trim().isEmpty()) {
			throw new InvalidTriviaConfigurationException(
					"The server name field is required, and is empty in the provided configuration");
		}
	}

	public static void validateServerOrChannelId(String id, String whichId) throws InvalidTriviaConfigurationException {
		if (id == null || id.trim().isEmpty()) {
			throw new InvalidTriviaConfigurationException(
					"The " + whichId + " id field is required, and is empty in the provided configuration");
		}
		try {
			Long.parseLong(id);
		} catch (NumberFormatException ex) {
			throw new InvalidTriviaConfigurationException(
					"The " + whichId + " id field is not valid (should be a long, but could not be parsed as a long)");
		}
	}

	public static void validateWaitTime(int waitTime, Mode mode) throws InvalidTriviaConfigurationException {
		if (waitTime < 1 && mode != Mode.MANUAL) {
			throw new InvalidTriviaConfigurationException("The wait time cannot be less than 1");
		}
	}

	public static void validateQuestions(List<TriviaQuestion> questions) throws InvalidTriviaConfigurationException {
		for (TriviaQuestion question : questions) {
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
