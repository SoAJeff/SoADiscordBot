package com.soa.rs.discordbot.bot.events.trivia;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import com.soa.rs.discordbot.bot.events.trivia.AutomatedTrivia;
import com.soa.rs.discordbot.bot.events.trivia.TriviaManager;
import com.soa.rs.triviacreator.jaxb.QuestionBank;
import com.soa.rs.triviacreator.jaxb.TriviaAnswers;
import com.soa.rs.triviacreator.jaxb.TriviaConfiguration;
import com.soa.rs.triviacreator.jaxb.TriviaQuestion;
import com.soa.rs.triviacreator.util.InvalidTriviaConfigurationException;

import sx.blah.discord.api.IDiscordClient;

public class SoaTriviaTest {

	private IDiscordClient client;
	private TriviaBase trivia;
	private TriviaConfiguration configuration;
	private TriviaManager manager = new TriviaManager();

	@Before
	public void createTrivia() {
		this.trivia = new AutomatedTrivia(client);

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

		this.trivia.setConfiguration(configuration);
	}

	@Test
	public void submitSingleQuestionAnswerTest() {
		this.trivia.initializeAnswersDoc();
		TriviaAnswers answers = this.trivia.getAnswersDoc();

		answers.getAnswerBank().getTriviaQuestion()
				.add(this.trivia.createQuestionAndAnswer(
						this.trivia.getConfiguration().getQuestionBank().getTriviaQuestion().get(0).getQuestion(),
						this.trivia.getConfiguration().getQuestionBank().getTriviaQuestion().get(0).getAnswer()));
		this.trivia.setAnswersDoc(answers);

		this.trivia.submitAnswer("Jeff", "Answer 1 to Question 1");
		this.trivia.submitAnswer("Josh", "Answer 2 to Question 1");

		// Check to make sure the question and the answer match
		Assert.assertEquals(this.trivia.getAnswersDoc().getAnswerBank().getTriviaQuestion().get(0).getQuestion(),
				"What is question 1?");
		Assert.assertEquals(this.trivia.getAnswersDoc().getAnswerBank().getTriviaQuestion().get(0).getCorrectAnswer(),
				"This is question 1");

		// Check to make sure the submitted answers match
		Assert.assertEquals(this.trivia.getAnswersDoc().getAnswerBank().getTriviaQuestion().get(0).getAnswers()
				.getParticipant().get(0).getParticipantAnswer(), "Answer 1 to Question 1");
		Assert.assertEquals(this.trivia.getAnswersDoc().getAnswerBank().getTriviaQuestion().get(0).getAnswers()
				.getParticipant().get(1).getParticipantAnswer(), "Answer 2 to Question 1");

	}

	@Test
	public void checkValidConfiguration() {
		boolean valid = true;
		try {

			manager.validateConfiguration(configuration);
		} catch (InvalidTriviaConfigurationException e) {
			valid = false;
		}
		Assert.assertEquals(valid, true);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testNullTriviaName() throws InvalidTriviaConfigurationException {
		String nullString = null;
		configuration.setTriviaName(nullString);
		manager.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testEmptyTriviaName() throws InvalidTriviaConfigurationException {
		configuration.setTriviaName("");
		manager.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testNullServerId() throws InvalidTriviaConfigurationException {
		String nullString = null;
		configuration.setServerId(nullString);
		manager.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testEmptyServerId() throws InvalidTriviaConfigurationException {
		configuration.setServerId("");
		manager.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testInvalidServerId() throws InvalidTriviaConfigurationException {
		configuration.setServerId("wat");
		manager.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testNullChannelId() throws InvalidTriviaConfigurationException {
		String nullString = null;
		configuration.setChannelId(nullString);
		manager.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testEmptyChannelId() throws InvalidTriviaConfigurationException {
		configuration.setChannelId("");
		manager.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testInvalidChannelId() throws InvalidTriviaConfigurationException {
		configuration.setChannelId("wat");
		manager.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testNegativeWaitTime() throws InvalidTriviaConfigurationException {
		configuration.setWaitTime(-1);
		manager.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testZeroWaitTime() throws InvalidTriviaConfigurationException {
		configuration.setWaitTime(0);
		manager.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testNullQuestion() throws InvalidTriviaConfigurationException {
		String nullString = null;
		configuration.getQuestionBank().getTriviaQuestion().get(0).setQuestion(nullString);
		manager.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testEmptyQuestion() throws InvalidTriviaConfigurationException {
		configuration.getQuestionBank().getTriviaQuestion().get(0).setQuestion("");
		manager.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testNullAnswer() throws InvalidTriviaConfigurationException {
		String nullString = null;
		configuration.getQuestionBank().getTriviaQuestion().get(0).setAnswer(nullString);
		manager.validateConfiguration(configuration);
	}

	@Test(expected = InvalidTriviaConfigurationException.class)
	public void testEmptyAnswer() throws InvalidTriviaConfigurationException {
		configuration.getQuestionBank().getTriviaQuestion().get(0).setAnswer("");
		manager.validateConfiguration(configuration);
	}
}
