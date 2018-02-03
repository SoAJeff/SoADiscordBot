package com.soa.rs.discordbot.bot.events.trivia;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

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

/**
 * The base implementation of Trivia. All trivia implementations must extend
 * this base class to be compatible with the TriviaManager.
 */
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
	private long triviaMaster = -1;

	/**
	 * An object representing a Trivia Question; contains a question and its
	 * associated correct answer
	 */
	private TriviaQuestion question;

	/**
	 * The Answers document for holding all submitted answers.
	 */
	private TriviaAnswers answersDoc;

	/**
	 * Boolean detailing whether trivia is currently running or not.
	 */
	private boolean triviaEnabled = false;

	/**
	 * A constant string to be used for indicating how to answer a question
	 */
	private final String answerFormat = "PM your answers to me, beginning your answer with \".trivia answer\". \nThe question is: ";

	/**
	 * Boolean detailing whether answers for questions are currently being accepted.
	 */
	protected boolean acceptAnswers = false;

	/**
	 * Constructor
	 *
	 * @param client
	 *            Discord Client in use
	 */
	public TriviaBase(IDiscordClient client) {
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
			while (!checkAdvance())
				Thread.sleep(1000);
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

		while (this.triviaEnabled && questions.hasNext()) {
			if (!checkStatus())
				return;
			acceptAnswers = false;
			messageChannel(getEndOfQuestionString() + question.getAnswer());

			if (!checkAdvance())
				return;

			if (questions.hasNext()) {
				question = questions.next();
				messageChannel("The next question is: " + question.getQuestion());
				this.answersDoc.getAnswerBank().getTriviaQuestion()
						.add(createQuestionAndAnswer(question.getQuestion(), question.getAnswer()));

			}
		}

		// Last question

		if (!checkStatus())
			return;
		messageChannel(getEndOfQuestionString() + question.getAnswer());

		try {
			exportAnswersToTriviaMaster();
		} catch (IOException e) {
			SoaLogging.getLogger().error("Error exporting answers", e);
		}
		this.triviaEnabled = false;
		SoaLogging.getLogger().info("Trivia has ended as  all questions have been asked.");
	}

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
		acceptAnswers = true;
		return newQuestion;
	}

	/**
	 * Submits an answer to the answers document
	 *
	 * @param user
	 *            The user submitting the answer
	 * @param answer
	 *            The answer text
	 * @return True if the answer was accepted; false if answers are not currently
	 *         being accepted.
	 */
	public boolean submitAnswer(String user, String answer) {
		if (acceptAnswers) {
			Participant participant = new Participant();
			participant.setParticipantName(user);
			participant.setParticipantAnswer(answer);
			int questionSize = this.answersDoc.getAnswerBank().getTriviaQuestion().size();
			this.answersDoc.getAnswerBank().getTriviaQuestion().get(questionSize - 1).getAnswers().getParticipant()
					.add(participant);
			return true;
		} else
			return false;
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
			if (dataStream != null) {
				dataStream.close();
			}
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
	 * Sends a list of who has answered the question to the triviamaster.
	 *
	 * @return String of content to send
	 */
	public String sendPlayersWhoAnsweredCurrentQuestion() {
		int questionSize = this.answersDoc.getAnswerBank().getTriviaQuestion().size();
		List<Participant> participants = this.answersDoc.getAnswerBank().getTriviaQuestion().get(questionSize - 1)
				.getAnswers().getParticipant();
		StringBuilder sb = new StringBuilder();
		sb.append("The following people have answered the current question:\n");
		for (Participant participant : participants) {
			sb.append(participant.getParticipantName());
			sb.append("\n");
		}
		return sb.toString();
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
	 * Submit a message to the channel trivia is being played in
	 *
	 * @param content
	 */
	protected void messageChannel(String content) {
		SoaClientHelper.sendMsgToChannel(this.client.getChannelByID(Long.parseLong(this.configuration.getChannelId())),
				content);
	}

	/**
	 * Checks if the trivia thread can advance to the next question
	 *
	 * @return true if can advance, false otherwise
	 */
	protected abstract boolean checkAdvance();

	/**
	 * Checks if the trivia thread should continue waiting and accepting answers to
	 * the question
	 *
	 * @return true if can advance, false otherwise
	 */
	protected abstract boolean checkStatus();

	/**
	 * Get the "end of answer period" string. The string to use will be defined by
	 * the implementing subclass
	 *
	 * @return The string to use when the answer period for a question has ended.
	 */
	protected abstract String getEndOfQuestionString();

	/**
	 * Handle arguments specific to this implementation of Trivia.
	 *
	 * @param args
	 * @param msg
	 */
	protected abstract void handleAdditionalArgs(String[] args, IMessage msg);

	/**
	 * Adds implementation specific arguments to the help line.
	 *
	 * @param sb
	 *            StringBuilder containing current help menu to be added to
	 */
	protected abstract void addAdditionalArgsToHelp(StringBuilder sb);

}
