package com.soa.rs.triviacreator.gui.answer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.soa.rs.triviacreator.jaxb.Participant;

public class TriviaAnswerTableModel extends AbstractTableModel {

	private List<Participant> answers = new ArrayList<>();

	private String[] columnNames = { "Participant", "Answer" };

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return answers.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return answers.get(rowIndex).getParticipantName();
		} else {
			return answers.get(rowIndex).getParticipantAnswer();
		}
	}

	public void setAnswers(List<Participant> answers) {
		this.answers.clear();
		for (Participant participant : answers) {
			this.answers.add(participant);
		}
		fireTableDataChanged();
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
