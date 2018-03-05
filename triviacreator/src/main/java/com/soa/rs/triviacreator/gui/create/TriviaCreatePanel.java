package com.soa.rs.triviacreator.gui.create;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.soa.rs.triviacreator.jaxb.Mode;
import com.soa.rs.triviacreator.util.DiscordType;
import com.soa.rs.triviacreator.util.InvalidTriviaConfigurationException;
import com.soa.rs.triviacreator.util.StringToDiscordPair;

public class TriviaCreatePanel extends JPanel implements TriviaCreateControllerListener, TriviaPanel {

	private TriviaCreateController controller;
	private JTextField triviaNameField;
	private JComboBox<StringToDiscordPair> serverName;
	private JComboBox<StringToDiscordPair> channelName;
	private List<JRadioButton> buttonList = new ArrayList<>();
	private JTextField waitTimeField;
	private JTextField forumUrl;
	private TriviaQuestionTableModel tableModel;
	private JTable questionTable;
	private JButton editButton = new JButton("Edit Question");
	private JButton deleteButton = new JButton("Delete Question");
	private Border triviaNameBorder;
	private Border waitTimeBorder;

	public TriviaCreatePanel(TriviaCreateController controller) {
		this.controller = controller;
		this.controller.addListener(this);
		this.setLayout(new BorderLayout());
	}

	@Override
	public JPanel createPanel() {
		this.add(buildDataEntryPanel(), BorderLayout.NORTH);
		this.add(buildQuestionsPanel(), BorderLayout.CENTER);
		return this;
	}

	private JPanel buildDataEntryPanel() {
		JPanel panel = new JPanel();
		TitledBorder border = BorderFactory.createTitledBorder("Trivia Configuration");
		panel.setBorder(border);
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.weightx = 0;

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(new JLabel("Trivia Name: "), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		this.triviaNameField = new JTextField();
		this.triviaNameBorder = this.triviaNameField.getBorder();
		this.triviaNameField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {

			}

			@Override
			public void focusLost(FocusEvent e) {
				try {
					controller.setTriviaName(triviaNameField.getText());
					triviaNameField.setToolTipText("");
					triviaNameField.setBorder(triviaNameBorder);
				} catch (InvalidTriviaConfigurationException ex) {
					triviaNameField.setToolTipText(ex.getMessage());
					setErrorBorder(triviaNameField);
				}
			}
		});
		panel.add(this.triviaNameField, gbc);

		gbc.gridx++;
		gbc.weightx = .5;

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		panel.add(new JLabel("Server: "), gbc);

		gbc.gridx++;
		gbc.weightx = 1;
		this.serverName = new JComboBox<>();
		this.serverName.addItem(new StringToDiscordPair("Spirits of Arianwyn", "133922153010692096"));
		this.serverName.addItem(new StringToDiscordPair("(new)", null));
		this.serverName.setEditable(false);
		this.serverName.setToolTipText("If the Server is not in the list, select (new) to add it");
		this.serverName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				StringToDiscordPair pair = (StringToDiscordPair) serverName.getSelectedItem();
				if (pair.getName().equals("(new)")) {
					CreateDiscordPairDialog dialog = new CreateDiscordPairDialog(
							(JFrame) SwingUtilities.getWindowAncestor(panel), "Add Server", DiscordType.SERVER);
					if (dialog.getPair() != null) {
						serverName.insertItemAt(dialog.getPair(), serverName.getItemCount() - 1);
						serverName.setSelectedIndex(serverName.getItemCount() - 2);
						controller.setServerId(dialog.getPair().getId());
					}
				} else {
					controller.setServerId(pair.getId());
				}

			}
		});
		this.controller.setServerId(((StringToDiscordPair) this.serverName.getSelectedItem()).getId());
		panel.add(this.serverName, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		panel.add(new JLabel("Channel: "), gbc);

		gbc.gridx++;
		gbc.weightx = 1;

		this.channelName = new JComboBox<>();
		this.channelName.addItem(new StringToDiscordPair("#events", "133942883274326016"));
		this.channelName.addItem(new StringToDiscordPair("#shoutbox", "133922153010692096"));
		this.channelName.addItem(new StringToDiscordPair("(new)", null));
		this.channelName.setEditable(false);
		this.channelName.setToolTipText("If the Channel is not in the list, select (new) to add it");
		this.channelName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				StringToDiscordPair pair = (StringToDiscordPair) channelName.getSelectedItem();
				if (pair.getName().equals("(new)")) {
					CreateDiscordPairDialog dialog = new CreateDiscordPairDialog(
							(JFrame) SwingUtilities.getWindowAncestor(panel), "Add Channel", DiscordType.CHANNEL);
					if (dialog.getPair() != null) {
						channelName.insertItemAt(dialog.getPair(), channelName.getItemCount() - 1);
						channelName.setSelectedIndex(channelName.getItemCount() - 2);
						controller.setChannelId(dialog.getPair().getId());
					}
				} else {
					controller.setChannelId(pair.getId());
				}

			}

		});
		this.controller.setChannelId(((StringToDiscordPair) this.channelName.getSelectedItem()).getId());
		panel.add(this.channelName, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.gridwidth = 3;

		panel.add(createRadioButtonPanel(), gbc);

		gbc.gridwidth = 1;
		gbc.gridy++;
		panel.add(new JLabel("Wait Time (seconds): "), gbc);

		gbc.gridx++;
		gbc.weightx = 1;
		this.waitTimeField = new JTextField();
		this.waitTimeBorder = this.waitTimeField.getBorder();
		this.waitTimeField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {

			}

			@Override
			public void focusLost(FocusEvent e) {
				if (waitTimeField.isEnabled()) {
					if (controller.getMode() == Mode.AUTOMATED) {
						try {
							controller.setWaitTime(Integer.parseInt(waitTimeField.getText()));
							waitTimeField.setToolTipText("");
							waitTimeField.setBorder(waitTimeBorder);
						} catch (InvalidTriviaConfigurationException ex) {
							waitTimeField.setToolTipText(ex.getMessage());
							setErrorBorder(waitTimeField);
						} catch (NumberFormatException ex) {
							waitTimeField.setToolTipText("Wait Time must be a valid positive integer");
							setErrorBorder(waitTimeField);
						}
					} else {
						waitTimeField.setToolTipText("");
						waitTimeField.setBorder(waitTimeBorder);
					}

				}
			}
		});
		panel.add(this.waitTimeField, gbc);

		gbc.gridx++;
		gbc.weightx = .5;

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;

		panel.add(new JLabel("Forum URL For Event: "), gbc);

		gbc.gridx++;
		gbc.weightx = 1;
		forumUrl = new JTextField();
		forumUrl.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {

			}

			@Override
			public void focusLost(FocusEvent e) {
				controller.setForumUrl(forumUrl.getText());
			}
		});
		panel.add(forumUrl, gbc);

		return panel;
	}

	private JPanel createRadioButtonPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, Mode.values().length));
		panel.add(new JLabel("Trivia Mode: "));

		ButtonGroup modeGroup = new ButtonGroup();

		for (Mode mode : Mode.values()) {
			JRadioButton btn = new JRadioButton(mode.name());
			btn.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (btn.isSelected()) {
						controller.setMode(Mode.fromValue(btn.getText()));
						if (btn.getText().equals("MANUAL")) {
							waitTimeField.setEnabled(false);
							waitTimeField.setBorder(waitTimeBorder);
						} else {
							waitTimeField.setEnabled(true);
							waitTimeField.setBorder(waitTimeBorder);
						}

					}
				}
			});
			modeGroup.add(btn);
			panel.add(btn);
			buttonList.add(btn);
		}
		return panel;
	}

	private JPanel buildQuestionsPanel() {
		JPanel panel = new JPanel();
		TitledBorder border = BorderFactory.createTitledBorder("Trivia Questions");
		panel.setBorder(border);

		panel.setLayout(new BorderLayout());
		JPanel questionPanel = new JPanel();
		questionPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		tableModel = new TriviaQuestionTableModel();
		questionTable = new JTable();
		questionTable.setModel(tableModel);
		questionTable.createDefaultColumnsFromModel();
		questionTable.setColumnSelectionAllowed(false);
		questionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionModel selectionModel = questionTable.getSelectionModel();
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (selectionModel.isSelectionEmpty()) {
					editButton.setEnabled(false);
					deleteButton.setEnabled(false);
				} else {
					editButton.setEnabled(true);
					deleteButton.setEnabled(true);
				}
			}
		});
		JScrollPane pane = new JScrollPane();
		pane.getViewport().add(questionTable);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.insets = new Insets(5, 5, 5, 5);
		questionPanel.add(pane, gbc);
		gbc.anchor = GridBagConstraints.SOUTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridheight = 1;
		gbc.weighty = 1;
		gbc.weightx = 0;
		gbc.gridx++;
		JButton moveUpButton = new JButton("Move Up");
		moveUpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (questionTable.getSelectedRow() != -1) {
					tableModel.moveQuestionUp(questionTable.getSelectedRow());
				}
			}
		});

		questionPanel.add(moveUpButton, gbc);

		gbc.gridy++;
		gbc.anchor = GridBagConstraints.NORTH;
		JButton moveDownButton = new JButton("Move Down");
		moveDownButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (questionTable.getSelectedRow() != -1) {
					tableModel.moveQuestionDown(questionTable.getSelectedRow());
				}
			}
		});
		questionPanel.add(moveDownButton, gbc);

		panel.add(questionPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 3));

		JButton addButton = new JButton("Add Question");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TriviaQuestionEditorDialog dialog = new TriviaQuestionEditorDialog(
						(JFrame) SwingUtilities.getWindowAncestor(panel));
				if (dialog.getQuestion() != null) {
					tableModel.addQuestion(dialog.getQuestion());
				}
			}
		});
		buttonPanel.add(addButton);

		editButton.setEnabled(false);
		editButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (questionTable.getSelectedRow() != -1) {
					TriviaQuestionEditorDialog dialog = new TriviaQuestionEditorDialog(
							tableModel.getQuestions().get(questionTable.getSelectedRow()),
							(JFrame) SwingUtilities.getWindowAncestor(panel));
					tableModel.swapQuestionAt(questionTable.getSelectedRow(), dialog.getQuestion());
				}
			}
		});
		buttonPanel.add(editButton);

		deleteButton.setEnabled(false);
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (questionTable.getSelectedRow() != -1) {
					int confirm_deletion = JOptionPane.showConfirmDialog(null,
							"Are you sure you want to delete this question?", "Confirm delete",
							JOptionPane.YES_NO_OPTION);
					if (confirm_deletion == JOptionPane.YES_OPTION) {
						tableModel.removeQuestionAt(questionTable.getSelectedRow());
					}
				}
			}
		});
		buttonPanel.add(deleteButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		return panel;
	}

	@Override
	public void updateSave() {
		JOptionPane.showMessageDialog(null, "Save successful!", "Save Successful!", JOptionPane.INFORMATION_MESSAGE);

	}

	@Override
	public void updateLoad() {
		populateData();
	}

	private void populateData() {

		this.triviaNameField.setText(this.controller.getTriviaName());
		setComboBox(this.serverName, this.controller.getServerId());
		setComboBox(this.channelName, this.controller.getChannelId());
		for (JRadioButton btn : buttonList) {
			if (btn.getText().equals(this.controller.getMode().toString()))
				btn.setSelected(true);
		}
		this.waitTimeField.setText(Integer.toString(this.controller.getWaitTime()));
		this.tableModel.setQuestions(this.controller.getQuestions());

	}

	private void setComboBox(JComboBox<StringToDiscordPair> box, String id) {
		boolean itemFound = false;
		for (int i = 0; i < box.getItemCount(); i++) {
			if (id.equals(box.getItemAt(i).getId())) {
				box.setSelectedIndex(i);
				itemFound = true;
				break;
			}
		}
		if (!itemFound) {
			box.addItem(new StringToDiscordPair(id, id));
		}
	}

	@Override
	public void notifyError(String error) {
		JOptionPane.showMessageDialog(this, "An error was encountered: " + error, "Error", JOptionPane.ERROR_MESSAGE);

	}

	private void setErrorBorder(JComponent component) {
		component.setBorder(BorderFactory.createLineBorder(Color.RED));
	}

	@Override
	public void handleLoad(File file) {
		this.controller.setFile(file);
		this.controller.load();

	}

	@Override
	public void handleSave() {
		try {
			this.controller.setQuestions(this.tableModel.getQuestions());
			this.controller.save();
		} catch (Exception e) {
			notifyError(e.getMessage());
		}

	}

	@Override
	public void handleSaveAs(File file) {
		this.controller.setFile(file);
		handleSave();
	}

}
