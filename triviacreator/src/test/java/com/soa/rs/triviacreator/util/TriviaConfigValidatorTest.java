package com.soa.rs.triviacreator.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.soa.rs.triviacreator.jaxb.QuestionBank;
import com.soa.rs.triviacreator.jaxb.TriviaConfiguration;
import com.soa.rs.triviacreator.jaxb.TriviaQuestion;

public class TriviaConfigValidatorTest {

	private TriviaConfiguration configuration;

	@Before
	public void createTrivia() {
		configuration = new TriviaConfiguration();
		configuration.setTriviaName("Test trivia name");
		configuration.setServerId("252267969617461248");
		configuration.setChannelId("252267969617461248");
		configuration.setWaitTime(5);
		configuration.setForumUrl("https://forums.soa-rs.com");
		QuestionBank bank = new QuestionBank();
		TriviaQuestion question = new TriviaQuestion();
		question.setQuestion("What is question 1?");
		question.setAnswer("This is question 1");
		bank.getTriviaQuestion().add(question);
		question = new TriviaQuestion();
		question.setQuestion("What is question 2?");
		question.setAnswer("This is question 2");
		bank.getTriviaQuestion().add(question);
		configuration.setQuestionBank(bank);

	}

	@Test
	public void checkValidConfiguration() {
		boolean valid = true;
		try {

			TriviaConfigValidator.validateConfiguration(configuration);
		} catch (InvalidTriviaConfigurationException e) {
			valid = false;
		}
		Assert.assertEquals(valid, true);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testNullTriviaName() throws InvalidTriviaConfigurationException {
		String nullString = null;
		configuration.setTriviaName(nullString);
		TriviaConfigValidator.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testEmptyTriviaName() throws InvalidTriviaConfigurationException {
		configuration.setTriviaName("");
		TriviaConfigValidator.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testNullServerId() throws InvalidTriviaConfigurationException {
		String nullString = null;
		configuration.setServerId(nullString);
		TriviaConfigValidator.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testEmptyServerId() throws InvalidTriviaConfigurationException {
		configuration.setServerId("");
		TriviaConfigValidator.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testInvalidServerId() throws InvalidTriviaConfigurationException {
		configuration.setServerId("wat");
		TriviaConfigValidator.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testNullChannelId() throws InvalidTriviaConfigurationException {
		String nullString = null;
		configuration.setChannelId(nullString);
		TriviaConfigValidator.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testEmptyChannelId() throws InvalidTriviaConfigurationException {
		configuration.setChannelId("");
		TriviaConfigValidator.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testInvalidChannelId() throws InvalidTriviaConfigurationException {
		configuration.setChannelId("wat");
		TriviaConfigValidator.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testNegativeWaitTime() throws InvalidTriviaConfigurationException {
		configuration.setWaitTime(-1);
		TriviaConfigValidator.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testZeroWaitTime() throws InvalidTriviaConfigurationException {
		configuration.setWaitTime(0);
		TriviaConfigValidator.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testNullQuestion() throws InvalidTriviaConfigurationException {
		String nullString = null;
		configuration.getQuestionBank().getTriviaQuestion().get(0).setQuestion(nullString);
		TriviaConfigValidator.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testEmptyQuestion() throws InvalidTriviaConfigurationException {
		configuration.getQuestionBank().getTriviaQuestion().get(0).setQuestion("");
		TriviaConfigValidator.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testNullAnswer() throws InvalidTriviaConfigurationException {
		String nullString = null;
		configuration.getQuestionBank().getTriviaQuestion().get(0).setAnswer(nullString);
		TriviaConfigValidator.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testEmptyAnswer() throws InvalidTriviaConfigurationException {
		configuration.getQuestionBank().getTriviaQuestion().get(0).setAnswer("");
		TriviaConfigValidator.validateConfiguration(configuration);
	}
}
