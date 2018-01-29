package com.soa.rs.discordbot.bot.events.trivia;

import com.soa.rs.discordbot.util.SoaClientHelper;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Extends the TriviaBase class; this implementation of Trivia requires the
 * Trivia Master to manually advance the questions, rather than relying on
 * timers.
 */
public class ManualTrivia extends TriviaBase {

	/**
	 * Boolean for determining when to advance to the next question
	 */
	private boolean advance = false;

	/**
	 * Basic constructor
	 * 
	 * @param client
	 *            The client representing the Discord API
	 */
	public ManualTrivia(IDiscordClient client) {
		super(client);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.soa.rs.discordbot.bot.events.trivia.TriviaBase#checkAdvance()
	 */
	@Override
	protected boolean checkAdvance() {
		try {
			if (!advance) {
				SoaClientHelper.sendMessageToUser(getTriviaMaster(), client, "Trivia waiting for advance...");
			}
			while (!advance) {
				if (!isEnabled())
					return false;
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			return false;
		}
		if (!isEnabled())
			return false;
		else {
			advance = false;
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.soa.rs.discordbot.bot.events.trivia.TriviaBase#checkStatus()
	 */
	@Override
	protected boolean checkStatus() {
		return checkAdvance();
	}

	/**
	 * Advance to the next question
	 */
	private void advanceQuestion() {
		advance = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.soa.rs.discordbot.bot.events.trivia.TriviaBase#handleAdditionalArgs(java.
	 * lang.String[], sx.blah.discord.handle.obj.IMessage)
	 */
	@Override
	protected void handleAdditionalArgs(String[] args, IMessage msg) {
		if (args[1].equalsIgnoreCase("advance"))
			advanceQuestion();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.soa.rs.discordbot.bot.events.trivia.TriviaBase#getEndOfQuestionString()
	 */
	@Override
	public String getEndOfQuestionString() {
		return new String("Answers for this question have closed.  The correct answer was: ");
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
		sb.append("\nTrivia commands specific to Manual Trivia:\n");
		sb.append(
				".trivia advance* - Advance Trivia to the next Question, or close answers and display the correct answer.\n");

	}

}
