package com.soa.rs.discordbot.bot.events.trivia;

import com.soa.rs.discordbot.util.SoaClientHelper;
import com.soa.rs.discordbot.util.SoaLogging;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;

/**
 * The <tt>AutomatedTrivia</tt> class contains the runnable trivia thread, along
 * with various configuration parameters needed for executing trivia.
 */
public class AutomatedTrivia extends TriviaBase {

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
		super(client);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.soa.rs.discordbot.bot.events.trivia.TriviaBase#checkStatus()
	 */
	@Override
	protected boolean checkStatus() {
		try {
			/*
			 * Configuration stores number of seconds. Loop that number of times, sleeping 1
			 * second each time, and check if paused/stopped after each second passes.
			 */
			for (int i = 0; i < configuration.getWaitTime(); i++) {
				Thread.sleep(1000);
				while (isTriviaPaused()) {
					if (!isEnabled())
						return false;
					Thread.sleep(1000);
				}
			}
			if (!isEnabled())
				return false;
			else
				return true;
		} catch (InterruptedException e) {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.soa.rs.discordbot.bot.events.trivia.TriviaBase#checkAdvance()
	 */
	@Override
	protected boolean checkAdvance() {
		try {
			for (int i = 0; i < 3; i++) {
				while (isTriviaPaused()) {
					if (!isEnabled())
						return false;
					Thread.sleep(1000);
				}
				Thread.sleep(1000);
			}
			return true;
		} catch (InterruptedException e) {
			return false;
		}
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
	 * 
	 * @see
	 * com.soa.rs.discordbot.bot.events.trivia.Trivia#handleAdditionalArgs(java.lang
	 * .String[], sx.blah.discord.handle.obj.IMessage)
	 */
	@Override
	protected void handleAdditionalArgs(String[] args, IMessage msg) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.soa.rs.discordbot.bot.events.trivia.TriviaBase#getEndOfQuestionString()
	 */
	@Override
	public String getEndOfQuestionString() {
		return new String("Time's up!  The correct answer was: ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.soa.rs.discordbot.bot.events.trivia.TriviaBase#addAdditionalArgsToHelp(
	 * java.lang.StringBuilder)
	 */
	@Override
	protected void addAdditionalArgsToHelp(StringBuilder sb) {
		sb.append("\nTrivia commands specific to Automated Trivia:\n");
		sb.append(
				".trivia pause* & .trivia resume* - Pauses or resumes trivia.  Answers can still be collected while paused.\n");

	}

}
