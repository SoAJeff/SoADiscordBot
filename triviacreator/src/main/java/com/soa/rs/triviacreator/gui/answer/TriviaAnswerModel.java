package com.soa.rs.triviacreator.gui.answer;

import java.io.File;

import com.soa.rs.triviacreator.jaxb.TriviaAnswers;

public interface TriviaAnswerModel {
	
	public void addListener(TriviaAnswerMVCListener listener);
	
	public void removeListener(TriviaAnswerMVCListener listener);
	
	public void setFile(File file);
	
	public TriviaAnswers getAnswers();
	
	public void load();

}
