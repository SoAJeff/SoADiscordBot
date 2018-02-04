package com.soa.rs.triviacreator.gui.create;

import java.io.File;
import java.util.List;

import com.soa.rs.triviacreator.jaxb.Mode;
import com.soa.rs.triviacreator.jaxb.TriviaQuestion;
import com.soa.rs.triviacreator.util.InvalidTriviaConfigurationException;

public interface TriviaCreateController {
	
	/*
	 * Listeners
	 */
	
	public void addListener(TriviaCreateControllerListener listener);
	
	public void removeListener(TriviaCreateControllerListener listener);
	
	/*
	 * Sets and gets
	 */
	
	public void setTriviaName(String name) throws InvalidTriviaConfigurationException;
	
	public String getTriviaName();
	
	public void setServerId(String id)throws InvalidTriviaConfigurationException;
	
	public String getServerId();
	
	public void setChannelId(String id)throws InvalidTriviaConfigurationException;
	
	public String getChannelId();
	
	public void setMode(Mode mode);
	
	public Mode getMode();
	
	public void setWaitTime(int time)throws InvalidTriviaConfigurationException;
	
	public int getWaitTime();
	
	public void setForumUrl(String url);
	
	public String getForumUrl();
	
	public void setQuestions(List<TriviaQuestion> questions)throws InvalidTriviaConfigurationException;
	
	public List<TriviaQuestion> getQuestions();
	
	public void setFile(File file);
	
	public File getFile();
	
	public void save();
	
	public void load();

}
