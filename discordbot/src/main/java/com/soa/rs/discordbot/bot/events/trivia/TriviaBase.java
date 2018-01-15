package com.soa.rs.discordbot.bot.events.trivia;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.soa.rs.discordbot.util.SoaClientHelper;
import com.soa.rs.discordbot.util.SoaLogging;
import com.soa.rs.triviacreator.jaxb.AnswerBank;
import com.soa.rs.triviacreator.jaxb.Answers;
import com.soa.rs.triviacreator.jaxb.Participant;
import com.soa.rs.triviacreator.jaxb.Questions;
import com.soa.rs.triviacreator.jaxb.TriviaAnswers;
import com.soa.rs.triviacreator.jaxb.TriviaConfiguration;
import com.soa.rs.triviacreator.jaxb.TriviaQuestion;
import com.soa.rs.triviacreator.util.TriviaAnswersStreamWriter;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;

public abstract class TriviaBase implements Runnable {

	/**
	 * The client the bot is connected to for the Discord API.
	 */
	protected IDiscordClient client;

	/**
	 * The trivia configuration to be used
	 */
	protected TriviaConfiguration configuration;

	/**
	 * The Discord ID of the triviamaster who submitted the configuration
	 */
	protected long triviaMaster = -1;

	/**
	 * An object representing a Trivia Question; contains a question and its
	 * associated correct answer
	 */
	protected TriviaQuestion question;

	/**
	 * The Answers document for holding all submitted answers.
	 */
	protected TriviaAnswers answersDoc;

	/**
	 * Boolean detailing whether trivia is currently running or not.
	 */
	protected boolean triviaEnabled = false;

	/**
	 * A constant string to be used for indicating how to answer a question
	 */
	protected final String answerFormat = "PM your answers to me, beginning your answer with \".trivia answer\". \nThe question is: ";

	/**
	 * Toggle trivia as enabled or disabled
	 * 
	 * @param enable
	 *            Whether trivia should be enabled or disabled
	 */
	public void enableTrivia(boolean enable) {
		this.triviaEnabled = enable;
	}

	/**
	 * Check if trivia is enabled
	 * 
	 * @return true if enabled, false if not
	 */
	public boolean isEnabled() {
		return this.triviaEnabled;
	}

	/**
	 * Set the trivia configuration to be used.
	 * 
	 * @param configuration
	 *            the configuration to be used
	 */
	public void setConfiguration(TriviaConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Get the trivia configuration
	 * 
	 * @return The trivia configuration
	 */
	public TriviaConfiguration getConfiguration() {
		return this.configuration;
	}

	/**
	 * Get the trivia master
	 * 
	 * @return The trivia master's ID
	 */
	public long getTriviaMaster() {
		return this.triviaMaster;
	}

	/**
	 * Set the trivia master
	 * 
	 * @param triviaMaster
	 *            The trivia master's ID
	 */
	public void setTriviaMaster(long triviaMaster) {
		this.triviaMaster = triviaMaster;
	}

	/**
	 * Initialize the answers document
	 */
	protected void initializeAnswersDoc() {
		this.answersDoc = new TriviaAnswers();
		this.answersDoc.setTriviaName(this.configuration.getTriviaName());
		this.answersDoc.setAnswerBank(new AnswerBank());
	}

	/**
	 * Create a question and answer for use in the answers document
	 * 
	 * @param question
	 *            The question text
	 * @param answer
	 *            The answer text
	 * @return A trivia question object for the answers document
	 */
	protected Questions createQuestionAndAnswer(String question, String answer) {
		Questions newQuestion = new Questions();
		newQuestion.setQuestion(question);
		newQuestion.setCorrectAnswer(answer);
		newQuestion.setAnswers(new Answers());
		return newQuestion;
	}

	/**
	 * Submits an answer to the answers document
	 * 
	 * @param user
	 *            The user submitting the answer
	 * @param answer
	 *            The answer text
	 */
	public void submitAnswer(String user, String answer) {
		Participant participant = new Participant();
		participant.setParticipantName(user);
		participant.setParticipantAnswer(answer);
		int questionSize = this.answersDoc.getAnswerBank().getTriviaQuestion().size();
		this.answersDoc.getAnswerBank().getTriviaQuestion().get(questionSize - 1).getAnswers().getParticipant()
				.add(participant);
	}

	/**
	 * Exports the trivia answers to the triviamaster.
	 * 
	 * @throws IOException
	 *             If there is an error in writing to the stream
	 */
	public void exportAnswersToTriviaMaster() throws IOException {
		TriviaAnswersStreamWriter writer = new TriviaAnswersStreamWriter();
		InputStream dataStream = null;
		try {
			dataStream = writer.writeTriviaAnswersToStream(this.answersDoc);
			String filename = new String(this.configuration.getTriviaName() + ".xml");
			filename = filename.replaceAll(" ", "_");
			SoaClientHelper.sendMsgWithFileToUser(this.triviaMaster, client,
					"Trivia Answers file for Trivia: " + this.configuration.getTriviaName(), dataStream, filename);
		} catch (JAXBException | SAXException | IOException e) {
			SoaClientHelper.sendMessageToUser(this.triviaMaster, client,
					"Trivia Answers were unable to be provided due to an error; this should be reported to the developer.");
			SoaLogging.getLogger().error("Error sending Trivia Answers to the user: " + e.getMessage(), e);
		} finally {
			dataStream.close();
		}

	}

	/**
	 * Get the answer doc
	 * 
	 * @return The answer doc
	 */
	protected TriviaAnswers getAnswersDoc() {
		return answersDoc;
	}

	/**
	 * Set the answer doc
	 * 
	 * @param answersDoc
	 *            The answer doc
	 */
	protected void setAnswersDoc(TriviaAnswers answersDoc) {
		this.answersDoc = answersDoc;
	}

	/**
	 * Cleanup trivia; nulls out necessary values to prepare for another trivia
	 * session.
	 */
	protected void cleanupTrivia() {
		this.triviaMaster = -1;
		this.configuration = null;
		this.question = null;
		this.answersDoc = null;
		this.triviaEnabled = false;
	}

	/**
	 * Handle arguments specific to this implementation of Trivia.
	 * 
	 * @param args
	 * @param msg
	 */
	public abstract void handleAdditionalArgs(String[] args, IMessage msg);

}
