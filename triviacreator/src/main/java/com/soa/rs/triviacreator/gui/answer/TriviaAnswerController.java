package com.soa.rs.triviacreator.gui.answer;

import java.io.File;
import java.util.Set;

import com.soa.rs.triviacreator.jaxb.TriviaAnswers;

public interface TriviaAnswerController {

	public void addListener(TriviaAnswerMVCListener listener);

	public void removeListener(TriviaAnswerMVCListener listener);

	public void setFile(File file);

	public TriviaAnswers getAnswers();

	public int getTotalAnswersSubmitted();

	public void generateTotalParticipantSet();

	public int getTotalParticipants();

	public Set<String> getAllParticipantNames();

	public void load();

}
