package com.soa.rs.triviacreator.gui.create;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.soa.rs.triviacreator.jaxb.Mode;
import com.soa.rs.triviacreator.jaxb.QuestionBank;
import com.soa.rs.triviacreator.jaxb.TriviaConfiguration;
import com.soa.rs.triviacreator.jaxb.TriviaQuestion;
import com.soa.rs.triviacreator.util.TriviaFileReader;
import com.soa.rs.triviacreator.util.TriviaFileWriter;

public class DefaultTriviaCreateModel implements TriviaCreateModel {

	private List<TriviaCreateModelListener> listeners = new ArrayList<TriviaCreateModelListener>();
	private String name = null;
	private String serverId = null;
	private String channelId = null;
	private Mode mode;
	private int waitTime;
	private String forumUrl;
	private List<TriviaQuestion> questions = new ArrayList<TriviaQuestion>();
	private File file;

	@Override
	public void addListener(TriviaCreateModelListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(TriviaCreateModelListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void setTriviaName(String name) {
		this.name = name;
	}

	@Override
	public String getTriviaName() {
		return this.name;
	}

	@Override
	public void setServerId(String id) {
		this.serverId = id;
	}

	@Override
	public String getServerId() {
		return this.serverId;
	}

	@Override
	public void setChannelId(String id) {
		this.channelId = id;
	}

	@Override
	public String getChannelId() {
		return this.channelId;
	}

	@Override
	public void setMode(Mode mode) {
		this.mode = mode;
	}

	@Override
	public Mode getMode() {
		return this.mode;
	}

	@Override
	public void setWaitTime(int time) {
		this.waitTime = time;
	}

	@Override
	public int getWaitTime() {
		return this.waitTime;
	}

	@Override
	public void setForumUrl(String url) {
		this.forumUrl = url;
	}

	@Override
	public String getForumUrl() {
		return this.forumUrl;
	}

	@Override
	public void setQuestions(List<TriviaQuestion> questions) {
		this.questions = questions;
	}

	@Override
	public List<TriviaQuestion> getQuestions() {
		return this.questions;
	}

	@Override
	public void setFile(File file) {
		this.file = file;
	}

	@Override
	public File getFile() {
		return this.file;
	}

	@Override
	public void load() {
		TriviaFileReader reader = new TriviaFileReader();
		TriviaConfiguration config;
		try {
			config = reader.loadTriviaConfigFile(this.file);
		} catch (Exception e) {
			notifyLoadFailed(e.getCause().getMessage());
			return;
		}
		if (config != null) {
			this.name = config.getTriviaName();
			this.serverId = config.getServerId();
			this.channelId = config.getChannelId();
			this.mode = config.getMode();
			this.waitTime = config.getWaitTime();
			if (config.getForumUrl() != null || !config.getForumUrl().trim().isEmpty()) {
				if (config.getForumUrl() != null && !config.getForumUrl().trim().isEmpty()) {
					this.forumUrl = config.getForumUrl();
				}
				for (TriviaQuestion question : config.getQuestionBank().getTriviaQuestion()) {
					this.questions.add(question);
				}

			}
			notifyLoadCompleted();

		}
	}

	@Override
	public void save() {

		try {
			TriviaConfiguration config = new TriviaConfiguration();
			config.setTriviaName(this.name);
			config.setServerId(this.serverId);
			config.setChannelId(this.channelId);
			config.setMode(this.mode);
			config.setWaitTime(this.waitTime);
			if (this.forumUrl != null && !this.forumUrl.trim().isEmpty()) {
				config.setForumUrl(this.forumUrl);
			}
			config.setQuestionBank(new QuestionBank());
			for (TriviaQuestion question : this.questions) {
				config.getQuestionBank().getTriviaQuestion().add(question);
			}
			TriviaFileWriter writer = new TriviaFileWriter();

			writer.writeTriviaConfigFile(config, this.file);
		} catch (Exception e) {
			notifyFailedSave(e.getCause().getMessage());
		}
		notifySaveCompleted();

	}

	private void notifyFailedSave(String msg) {
		for (TriviaCreateModelListener listener : this.listeners) {
			listener.saveFailed(msg);
		}
	}

	private void notifySaveCompleted() {
		for (TriviaCreateModelListener listener : this.listeners) {
			listener.updateSave();
		}
	}

	private void notifyLoadCompleted() {
		for (TriviaCreateModelListener listener : this.listeners) {
			listener.updateLoad();
		}
	}

	private void notifyLoadFailed(String msg) {
		for (TriviaCreateModelListener listener : this.listeners) {
			listener.loadFailed(msg);
		}
	}

}
