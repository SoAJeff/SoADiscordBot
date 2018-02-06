package com.soa.rs.triviacreator.gui.create;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.soa.rs.triviacreator.jaxb.Mode;
import com.soa.rs.triviacreator.jaxb.TriviaQuestion;
import com.soa.rs.triviacreator.util.InvalidTriviaConfigurationException;
import com.soa.rs.triviacreator.util.TriviaConfigValidator;

public class DefaultTriviaCreateController implements TriviaCreateController, TriviaCreateModelListener {

	private TriviaCreateModel model;
	private List<TriviaCreateControllerListener> listeners = new ArrayList<TriviaCreateControllerListener>();

	public DefaultTriviaCreateController(TriviaCreateModel model) {
		this.model = model;
		this.model.addListener(this);
	}

	@Override
	public void addListener(TriviaCreateControllerListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener(TriviaCreateControllerListener listener) {
		this.listeners.remove(listener);
	}

	@Override
	public void setTriviaName(String name) throws InvalidTriviaConfigurationException {
		TriviaConfigValidator.validateTriviaName(name);
		this.model.setTriviaName(name);
	}

	@Override
	public String getTriviaName() {
		return this.model.getTriviaName();
	}

	@Override
	public void setServerId(String id) {
		this.model.setServerId(id);
	}

	@Override
	public String getServerId() {
		return this.model.getServerId();
	}

	@Override
	public void setChannelId(String id)  {
		this.model.setChannelId(id);
	}

	@Override
	public String getChannelId() {
		return this.model.getChannelId();
	}

	@Override
	public void setMode(Mode mode) {
		this.model.setMode(mode);
		System.out.println("Mode: " + mode.value());
	}

	@Override
	public Mode getMode() {
		return this.model.getMode();
	}

	@Override
	public void setWaitTime(int time) throws InvalidTriviaConfigurationException {
		TriviaConfigValidator.validateWaitTime(time, getMode());
		this.model.setWaitTime(time);
	}

	@Override
	public int getWaitTime() {
		return this.model.getWaitTime();
	}

	@Override
	public void setForumUrl(String url) {
		this.model.setForumUrl(url);
	}

	@Override
	public String getForumUrl() {
		return this.model.getForumUrl();
	}

	@Override
	public void setQuestions(List<TriviaQuestion> questions) throws InvalidTriviaConfigurationException {
		TriviaConfigValidator.validateQuestions(questions);
		this.model.setQuestions(questions);
	}

	@Override
	public List<TriviaQuestion> getQuestions() {
		return this.model.getQuestions();
	}

	@Override
	public void setFile(File file) {
		this.model.setFile(file);
	}

	@Override
	public File getFile() {
		return this.model.getFile();
	}
	
	@Override
	public void save() {
		this.model.save();
		
	}

	@Override
	public void load() {
		this.model.load();
		
	}

	@Override
	public void updateLoad() {
		for (TriviaCreateControllerListener listener : listeners) {
			listener.updateLoad();
		}
	}

	@Override
	public void updateSave() {
		for (TriviaCreateControllerListener listener : listeners) {
			listener.updateSave();
		}
	}

	@Override
	public void saveFailed(String msg) {
		for (TriviaCreateControllerListener listener : listeners) {
			listener.notifyError(msg);
		}
	}

	@Override
	public void loadFailed(String msg) {
		for (TriviaCreateControllerListener listener : listeners)
			listener.notifyError(msg);
	}


}
