package com.soa.rs.discordbot.bot.events.trivia;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.soa.rs.triviacreator.jaxb.QuestionBank;
import com.soa.rs.triviacreator.jaxb.TriviaAnswers;
import com.soa.rs.triviacreator.jaxb.TriviaConfiguration;
import com.soa.rs.triviacreator.jaxb.TriviaQuestion;

import sx.blah.discord.api.IDiscordClient;

public class SoaTriviaTest {

	private IDiscordClient client;
	private TriviaBase trivia;
	private TriviaConfiguration configuration;

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
	public void submitMultipleQuestionAnswerTest() {
		this.trivia.initializeAnswersDoc();
		TriviaAnswers answers = this.trivia.getAnswersDoc();

		answers.getAnswerBank().getTriviaQuestion()
				.add(this.trivia.createQuestionAndAnswer(
						this.trivia.getConfiguration().getQuestionBank().getTriviaQuestion().get(0).getQuestion(),
						this.trivia.getConfiguration().getQuestionBank().getTriviaQuestion().get(0).getAnswer()));
		this.trivia.setAnswersDoc(answers);

		this.trivia.submitAnswer("Jeff", "Answer 1 to Question 1");
		this.trivia.submitAnswer("Josh", "Answer 2 to Question 1");

		answers.getAnswerBank().getTriviaQuestion()
				.add(this.trivia.createQuestionAndAnswer(
						this.trivia.getConfiguration().getQuestionBank().getTriviaQuestion().get(1).getQuestion(),
						this.trivia.getConfiguration().getQuestionBank().getTriviaQuestion().get(1).getAnswer()));

		this.trivia.submitAnswer("Jeff", "Answer 1 to Question 2");
		this.trivia.submitAnswer("Josh", "Answer 2 to Question 2");

		Assert.assertEquals(this.trivia.getAnswersDoc().getAnswerBank().getTriviaQuestion().get(1).getAnswers()
				.getParticipant().get(0).getParticipantAnswer(), "Answer 1 to Question 2");
		Assert.assertEquals(this.trivia.getAnswersDoc().getAnswerBank().getTriviaQuestion().get(1).getAnswers()
				.getParticipant().get(1).getParticipantAnswer(), "Answer 2 to Question 2");

	}

	@Test
	public void testGetCurrentPlayersWhoRespondedToQuestionSingleQuestion() {
		this.trivia.initializeAnswersDoc();
		TriviaAnswers answers = this.trivia.getAnswersDoc();

		answers.getAnswerBank().getTriviaQuestion()
				.add(this.trivia.createQuestionAndAnswer(
						this.trivia.getConfiguration().getQuestionBank().getTriviaQuestion().get(0).getQuestion(),
						this.trivia.getConfiguration().getQuestionBank().getTriviaQuestion().get(0).getAnswer()));
		this.trivia.setAnswersDoc(answers);

		this.trivia.submitAnswer("Jeff", "Answer 1 to Question 1");
		this.trivia.submitAnswer("Josh", "Answer 2 to Question 1");

		String participants = this.trivia.sendPlayersWhoAnsweredCurrentQuestion();
		Assert.assertTrue(participants.contains("Jeff"));
		Assert.assertTrue(participants.contains("Josh"));
	}

	@Test
	public void testGetCurrentPlayersWhoRespondedToQuestionMultipleQuestion() {
		this.trivia.initializeAnswersDoc();

		TriviaAnswers answers = this.trivia.getAnswersDoc();

		answers.getAnswerBank().getTriviaQuestion()
				.add(this.trivia.createQuestionAndAnswer(
						this.trivia.getConfiguration().getQuestionBank().getTriviaQuestion().get(0).getQuestion(),
						this.trivia.getConfiguration().getQuestionBank().getTriviaQuestion().get(0).getAnswer()));
		this.trivia.setAnswersDoc(answers);

		this.trivia.submitAnswer("Jeff", "Answer 1 to Question 1");
		this.trivia.submitAnswer("Josh", "Answer 2 to Question 1");

		answers.getAnswerBank().getTriviaQuestion()
				.add(this.trivia.createQuestionAndAnswer(
						this.trivia.getConfiguration().getQuestionBank().getTriviaQuestion().get(1).getQuestion(),
						this.trivia.getConfiguration().getQuestionBank().getTriviaQuestion().get(1).getAnswer()));

		this.trivia.submitAnswer("Jeff", "Answer 1 to Question 2");

		String participants = this.trivia.sendPlayersWhoAnsweredCurrentQuestion();
		Assert.assertTrue(participants.contains("Jeff"));
		Assert.assertFalse(participants.contains("Josh"));

	}

	@Test
	public void testSubmitAnswersDisabled() {
		this.trivia.initializeAnswersDoc();

		TriviaAnswers answers = this.trivia.getAnswersDoc();

		answers.getAnswerBank().getTriviaQuestion()
				.add(this.trivia.createQuestionAndAnswer(
						this.trivia.getConfiguration().getQuestionBank().getTriviaQuestion().get(0).getQuestion(),
						this.trivia.getConfiguration().getQuestionBank().getTriviaQuestion().get(0).getAnswer()));
		this.trivia.setAnswersDoc(answers);

		Assert.assertTrue(this.trivia.submitAnswer("Jeff", "Answer 1 to Question 1"));

		this.trivia.acceptAnswers = false;

		Assert.assertFalse(this.trivia.submitAnswer("Josh", "Answer 2 to Question 1"));
	}


}
