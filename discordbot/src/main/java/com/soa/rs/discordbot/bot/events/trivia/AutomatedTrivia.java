package com.soa.rs.discordbot.bot.events.trivia;

import java.io.IOException;
import java.util.Iterator;

import com.soa.rs.discordbot.util.SoaClientHelper;
import com.soa.rs.discordbot.util.SoaLogging;
import com.soa.rs.triviacreator.jaxb.TriviaQuestion;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;

/**
 * The <tt>AutomatedTrivia</tt> class contains the runnable trivia thread, along
 * with various configuration parameters needed for executing trivia.
 */
public class AutomatedTrivia extends TriviaBase implements Runnable {

	/**
	 * A constant 'times up' string when announcing an answer
	 */
	private final String timesUp = "Time's up!  The answer was: ";

	/**
	 * Boolean detailing whether trivia currently is paused or not.
	 */
	private boolean triviaPaused = false;

	/**
	 * Basic constructor
	 * 
	 * @param client
	 *            The client representing the Discord API
	 */
	public AutomatedTrivia(IDiscordClient client) {
		this.client = client;
	}

	/**
	 * Execute trivia. This will start the trivia session and will periodically
	 * check to make sure it still should be running. Upon completion, it will
	 * export the answers to the triviamaster.
	 */
	@Override
	public void run() {
		SoaLogging.getLogger().info("Starting Trivia...");
		initializeAnswersDoc();

		try {
			StringBuilder sb = new StringBuilder();
			sb.append("Its Trivia Time! Welcome to " + configuration.getTriviaName());
			if (configuration.getForumUrl() != null && !configuration.getForumUrl().isEmpty()) {
				sb.append("\nThe forum thread for this event is: " + configuration.getForumUrl());
			}
			messageChannel(sb.toString());
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			this.triviaEnabled = false;
			return;
		}

		Iterator<TriviaQuestion> questions = this.configuration.getQuestionBank().getTriviaQuestion().iterator();
		question = questions.next();
		if (!this.isEnabled())
			return;
		messageChannel("Ready to play? " + answerFormat + question.getQuestion());

		this.answersDoc.getAnswerBank().getTriviaQuestion()
				.add(createQuestionAndAnswer(question.getQuestion(), question.getAnswer()));

		try {
			while (this.triviaEnabled && questions.hasNext()) {
				/*
				 * Configuration stores number of seconds. Loop that number of times, sleeping 1
				 * second each time, and check if paused/stopped after each second passes.
				 */
				for (int i = 0; i < configuration.getWaitTime(); i++) {
					Thread.sleep(1000); // 1 second
					if (!checkStatus())
						return;
				}
				messageChannel(timesUp + question.getAnswer());

				Thread.sleep(3000);
				if (!checkStatus())
					return;
				if (questions.hasNext()) {
					question = questions.next();
					messageChannel("The next question is: " + question.getQuestion());
					this.answersDoc.getAnswerBank().getTriviaQuestion()
							.add(createQuestionAndAnswer(question.getQuestion(), question.getAnswer()));

				}
			}

			// Last question
			for (int i = 0; i < configuration.getWaitTime(); i++) {
				Thread.sleep(1000);
				if (!checkStatus())
					return;
			}
			messageChannel(timesUp + question.getAnswer());

		} catch (InterruptedException e) {
			this.triviaEnabled = false;
			return;
		}
		try {
			exportAnswersToTriviaMaster();
		} catch (IOException e) {
			SoaLogging.getLogger().error("Error exporting answers", e);
		}
		this.triviaEnabled = false;
		SoaLogging.getLogger().info("Trivia has ended as  all questions have been asked.");
	}

	/**
	 * Check if the thread is currently
	 * 
	 * @return true if trivia is still enabled, false if not.
	 * @throws InterruptedException
	 *             if the thread has been stopped while paused. This interrupted
	 *             exception should be caught in the run method and used to stop the
	 *             thread, as it means trivia is no longer enabled.
	 */
	private boolean checkStatus() throws InterruptedException {
		while (isTriviaPaused()) {
			if (!isEnabled())
				return false;
			Thread.sleep(1000);
		}
		if (!isEnabled())
			return false;
		else
			return true;
	}

	/**
	 * Submit a message to the channel trivia is being played in
	 * 
	 * @param content
	 */
	private void messageChannel(String content) {
		SoaClientHelper.sendMsgToChannel(this.client.getChannelByID(Long.parseLong(this.configuration.getChannelId())),
				content);
	}

	/**
	 * Check if trivia is paused
	 * 
	 * @return true if paused, false if not
	 */
	public boolean isTriviaPaused() {
		return triviaPaused;
	}

	/**
	 * Set if trivia is paused
	 * 
	 * @param triviaPaused
	 *            Whether trivia should be paused or not. True indicates trivia
	 *            should be paused.
	 */
	public void setTriviaPaused(boolean triviaPaused) {
		this.triviaPaused = triviaPaused;
	}

	/**
	 * Cleanup trivia; nulls out necessary values to prepare for another trivia
	 * session.
	 */
	@Override
	public void cleanupTrivia() {
		super.cleanupTrivia();
		this.triviaPaused = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.soa.rs.discordbot.bot.events.trivia.TriviaBase#handleAdditionalArgs(java.
	 * lang.String[], sx.blah.discord.handle.obj.IMessage)
	 */
	@Override
	public void handleAdditionalArgs(String[] args, IMessage msg) {
		if (args[1].equalsIgnoreCase("pause")) {
			pauseTrivia(msg);
		} else if (args[1].equalsIgnoreCase("resume")) {
			resumeTrivia(msg);
		}

	}

	/**
	 * Pauses trivia
	 * 
	 * @param msg
	 */
	private void pauseTrivia(IMessage msg) {
		setTriviaPaused(true);
		SoaClientHelper.sendMsgToChannel(msg.getChannel(), "Trivia has been paused.");
		SoaLogging.getLogger().info("Trivia has been paused.");
	}

	/**
	 * Resumes trivia if it is paused
	 * 
	 * @param msg
	 */
	private void resumeTrivia(IMessage msg) {
		setTriviaPaused(false);
		SoaClientHelper.sendMsgToChannel(msg.getChannel(), "Trivia has been resumed.");
		SoaLogging.getLogger().info("Trivia has been resumed.");
	}

}
