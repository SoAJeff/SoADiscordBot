package com.soa.rs.triviacreator.gui.create;

import java.io.File;
import java.util.List;

import com.soa.rs.triviacreator.jaxb.Mode;
import com.soa.rs.triviacreator.jaxb.TriviaQuestion;

public interface TriviaCreateModel {
	
	/*
	 * Listeners
	 */
	
	public void addListener(TriviaCreateModelListener listener);
	
	public void removeListener(TriviaCreateModelListener listener);
	
	/*
	 * Sets and gets
	 */
	
	public void setTriviaName(String name);
	
	public String getTriviaName();
	
	public void setServerId(String id);
	
	public String getServerId();
	
	public void setChannelId(String id);
	
	public String getChannelId();
	
	public void setMode(Mode mode);
	
	public Mode getMode();
	
	public void setWaitTime(int time);
	
	public int getWaitTime();
	
	public void setForumUrl(String url);
	
	public String getForumUrl();
	
	public void setQuestions(List<TriviaQuestion> questions);
	
	public List<TriviaQuestion> getQuestions();
	
	public void setFile(File file);
	
	public File getFile();
	
	/*
	 * Actions
	 */
	
	public void load();
	
	public void save();
	
}
