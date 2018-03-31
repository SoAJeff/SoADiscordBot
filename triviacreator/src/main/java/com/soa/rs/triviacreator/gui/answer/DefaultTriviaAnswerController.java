package com.soa.rs.triviacreator.gui.answer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.soa.rs.triviacreator.jaxb.TriviaAnswers;

public class DefaultTriviaAnswerController implements TriviaAnswerController, TriviaAnswerMVCListener {

	private List<TriviaAnswerMVCListener> listeners = new ArrayList<>();
	private TriviaAnswerModel model;

	public DefaultTriviaAnswerController(TriviaAnswerModel model) {
		this.model = model;
		this.model.addListener(this);
	}

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
		this.model.setFile(file);
	}

	@Override
	public TriviaAnswers getAnswers() {
		return this.model.getAnswers();
	}

	@Override
	public void load() {
		this.model.load();
	}

	@Override
	public void loadCompleted() {
		synchronized (listeners) {
			for (TriviaAnswerMVCListener listener : this.listeners) {
				listener.loadCompleted();
			}
		}
	}

	@Override
	public void loadFailed(String message) {
		synchronized (listeners) {
			for (TriviaAnswerMVCListener listener : this.listeners) {
				listener.loadFailed(message);
			}
		}
	}

}
