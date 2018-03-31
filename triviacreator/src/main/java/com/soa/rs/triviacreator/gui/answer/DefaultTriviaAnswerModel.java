package com.soa.rs.triviacreator.gui.answer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.soa.rs.triviacreator.jaxb.TriviaAnswers;
import com.soa.rs.triviacreator.util.TriviaFileReader;

public class DefaultTriviaAnswerModel implements TriviaAnswerModel {

	private List<TriviaAnswerMVCListener> listeners = new ArrayList<>();
	private File file;
	private TriviaAnswers answers;

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
