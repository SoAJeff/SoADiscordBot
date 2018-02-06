package com.soa.rs.triviacreator.gui.create;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.soa.rs.triviacreator.jaxb.TriviaQuestion;

public class TriviaQuestionTableModel extends AbstractTableModel {

	private List<TriviaQuestion> questions = new ArrayList<>();

	private String[] columnNames = { "Question", "Answer" };

	@Override
	public int getRowCount() {
		return questions.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return questions.get(rowIndex).getQuestion();
		} else {
			return questions.get(rowIndex).getAnswer();
		}
	}

	public List<TriviaQuestion> getQuestions() {
		return this.questions;
	}

	public void setQuestions(List<TriviaQuestion> questions) {
		for (TriviaQuestion triviaQuestion : questions) {
			this.questions.add(triviaQuestion);

		}
		fireTableDataChanged();

	}

	public void addQuestion(TriviaQuestion question) {
		this.questions.add(question);
		fireTableDataChanged();
	}

	public void removeQuestionAt(int row) {
		this.questions.remove(row);
		fireTableDataChanged();
	}

	public void swapQuestionAt(int row, TriviaQuestion question) {
		this.questions.remove(row);
		this.questions.add(row, question);
		fireTableDataChanged();
	}

	public void moveQuestionUp(int position) {
		TriviaQuestion question = this.questions.get(position);
		try {
			this.questions.add(position - 1, question);
			this.questions.remove(position + 1);
		} catch (Exception e) {
		} finally {
			fireTableDataChanged();
		}

	}

	public void moveQuestionDown(int position) {
		TriviaQuestion question = this.questions.get(position);
		try {
			this.questions.add(position + 2, question);
			this.questions.remove(position);
		} catch (Exception e) {
		} finally {
			fireTableDataChanged();
		}

	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	@Override
	public Class getColumnClass(int col) {
		return String.class;
	}
}
