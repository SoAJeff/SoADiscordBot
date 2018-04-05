package com.soa.rs.triviacreator.gui.answer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.soa.rs.triviacreator.jaxb.Participant;
import com.soa.rs.triviacreator.jaxb.Questions;
import com.soa.rs.triviacreator.jaxb.TriviaAnswers;
import com.soa.rs.triviacreator.util.TriviaFileReader;

public class DefaultTriviaAnswerModel implements TriviaAnswerModel {

	private List<TriviaAnswerMVCListener> listeners = new ArrayList<>();
	private File file;
	private TriviaAnswers answers;
	private Set<String> participantSet = new TreeSet<>();

	@Override
	public void addListener(TriviaAnswerMVCListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener(TriviaAnswerMVCListener listener) {
		this.listeners.remove(listener);
	}

	@Override
	public void setFile(File file) {
		this.file = file;
	}

	@Override
	public TriviaAnswers getAnswers() {
		return this.answers;
	}

	@Override
	public int getTotalAnswersSubmitted() {
		int count = 0;
		for (Questions question : this.answers.getAnswerBank().getTriviaQuestion()) {
			count += question.getAnswers().getParticipant().size();
		}
		return count;
	}

	@Override
	public void generateTotalParticipantSet() {
		for (Questions question : this.answers.getAnswerBank().getTriviaQuestion()) {
			for (Participant participant : question.getAnswers().getParticipant()) {
				this.participantSet.add(participant.getParticipantName());
			}
		}
	}

	@Override
	public int getTotalParticipants() {
		return this.participantSet.size();
	}

	@Override
	public Set<String> getAllParticipantNames() {
		return this.participantSet;
	}

	@Override
	public void load() {
		try {
			TriviaFileReader reader = new TriviaFileReader();
			this.answers = reader.loadTriviaAnswerFile(this.file);
			notifyLoadCompleted();
		} catch (Exception e) {
			if (e.getMessage() != null) {
				notifyLoadFailed(e.getMessage());
			} else {
				notifyLoadFailed(e.getCause().getMessage());
			}
		}

	}

	private void notifyLoadCompleted() {
		synchronized (listeners) {
			for (TriviaAnswerMVCListener listener : this.listeners) {
				listener.loadCompleted();
			}
		}
	}

	private void notifyLoadFailed(String message) {
		synchronized (listeners) {
			for (TriviaAnswerMVCListener listener : this.listeners) {
				listener.loadFailed(message);
			}
		}
	}

}
