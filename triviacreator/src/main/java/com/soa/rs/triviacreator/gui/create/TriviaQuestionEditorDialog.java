package com.soa.rs.triviacreator.gui.create;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.soa.rs.triviacreator.jaxb.TriviaQuestion;

public class TriviaQuestionEditorDialog {

	private JTextField questionField;
	private JTextField answerField;
	private TriviaQuestion question = null;
	private JDialog dialog;

	public TriviaQuestionEditorDialog(JFrame frame) {
		initGui(frame);
	}

	public TriviaQuestionEditorDialog(TriviaQuestion question, JFrame frame) {
		this.question = question;
		initGui(frame);
	}

	public TriviaQuestion getQuestion() {
		return this.question;
	}

	private void initGui(JFrame frame) {
		dialog = new JDialog(frame, "Add/Edit Question", true);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(createTextPanel(), BorderLayout.NORTH);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);
		dialog.add(panel);
		this.dialog.pack();
		this.dialog.setLocationRelativeTo(frame);
		if (question != null) {
			populateGui();
		}
		this.dialog.setVisible(true);

	}

	private JPanel createTextPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = gbc.NORTHWEST;
		gbc.weightx = 3;
		gbc.insets = new Insets(5, 5, 5, 5);

		panel.add(new JLabel("Trivia Question: "), gbc);

		gbc.gridx++;
		gbc.weightx = 10;

		this.questionField = new JTextField(20);
		panel.add(this.questionField, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 3;
		panel.add(new JLabel("Trivia Answer: "), gbc);

		gbc.gridx++;
		gbc.weightx = 10;
		this.answerField = new JTextField(20);
		panel.add(this.answerField, gbc);

		return panel;
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				question = new TriviaQuestion();
				if (!questionField.getText().trim().isEmpty() && !questionField.getText().trim().isEmpty()) {
					question.setQuestion(questionField.getText());
					question.setAnswer(answerField.getText());
					dialog.dispose();
				} else {
					JOptionPane.showMessageDialog(dialog, "One or more required fields is empty", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		panel.add(okButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				dialog.dispose();
			}
		});
		panel.add(cancelButton);
		return panel;

	}

	private void populateGui() {
		this.questionField.setText(this.question.getQuestion());
		this.answerField.setText(this.question.getAnswer());
	}
}
