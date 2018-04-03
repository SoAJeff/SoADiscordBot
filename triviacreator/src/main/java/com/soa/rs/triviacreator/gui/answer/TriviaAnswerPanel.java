package com.soa.rs.triviacreator.gui.answer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.soa.rs.triviacreator.gui.TriviaPanel;
import com.soa.rs.triviacreator.jaxb.Questions;

public class TriviaAnswerPanel extends JPanel implements TriviaPanel, TriviaAnswerMVCListener {

	private TriviaAnswerController controller;
	private JLabel titleLabel;
	private JTree tree;
	private DefaultMutableTreeNode top;
	private DefaultTreeModel treeModel;
	private CardLayout cardLayout;
	private JPanel cardPanel;
	private TitledBorder questionBorder;
	private TriviaAnswerTableModel tableModel;
	private JTextArea questionArea = new JTextArea();
	private JTextArea answerArea = new JTextArea();
	private static final String SUMMARY = "Summary";
	private static final String ANSWERBANK = "Answer Bank";

	public TriviaAnswerPanel(TriviaAnswerController controller) {
		this.controller = controller;
		this.controller.addListener(this);
		this.setLayout(new BorderLayout());
	}

	@Override
	public JPanel createPanel() {
		this.add(createTitlePanel(), BorderLayout.NORTH);
		this.add(createTreePane(), BorderLayout.WEST);
		this.add(createCardPanel(), BorderLayout.CENTER);
		return this;
	}

	private JPanel createTitlePanel() {
		JPanel panel = new JPanel();
		titleLabel = new JLabel("Viewing Trivia answers for: ");
		panel.add(titleLabel);
		return panel;
	}

	private JScrollPane createTreePane() {
		top = new DefaultMutableTreeNode("Summary");
		tree = new JTree(top);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				TreePath path = e.getPath();
				int pathLength = path.getPathCount();
				if (pathLength == 1) {
					cardLayout.show(cardPanel, SUMMARY);
				} else {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
					int nodeNum = Integer.parseInt(node.toString().substring(node.toString().lastIndexOf(" ") + 1));
					populateCardPanel(nodeNum - 1);
					cardLayout.show(cardPanel, ANSWERBANK);
				}
			}
		});
		treeModel = new DefaultTreeModel(top);
		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setPreferredSize(new Dimension(250, 800));
		return scrollPane;
	}

	private JPanel createCardPanel() {
		cardPanel = new JPanel();
		cardLayout = new CardLayout();
		cardPanel.setLayout(cardLayout);

		cardPanel.add(createSummaryPanel(), SUMMARY);
		cardPanel.add(createAnswerBankPanel(), ANSWERBANK);

		return cardPanel;
	}

	private JPanel createSummaryPanel() {
		JPanel panel = new JPanel();
		return panel;
	}

	private JPanel createAnswerBankPanel() {
		JPanel questionPanel = new JPanel(new BorderLayout());
		questionBorder = new TitledBorder("Question ");
		questionPanel.setBorder(questionBorder);
		JPanel textPanel = new JPanel(new GridLayout(2, 1));
		initializeQandALabel(this.questionArea, "Question: ");
		initializeQandALabel(this.answerArea, "Answer: ");
		textPanel.add(this.questionArea);
		textPanel.add(this.answerArea);
		questionPanel.add(textPanel, BorderLayout.NORTH);

		tableModel = new TriviaAnswerTableModel();
		JTable answerTable = new JTable();
		answerTable.setModel(tableModel);
		answerTable.createDefaultColumnsFromModel();
		answerTable.setColumnSelectionAllowed(false);
		answerTable.setRowSelectionAllowed(false);
		answerTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JScrollPane pane = new JScrollPane();
		pane.getViewport().add(answerTable);
		questionPanel.add(pane, BorderLayout.CENTER);
		return questionPanel;
	}

	private void initializeQandALabel(JTextArea area, String text) {
		area.setText(text);
		area.setWrapStyleWord(true);
		area.setLineWrap(true);
		area.setOpaque(false);
		area.setEditable(false);
		area.setFocusable(false);
		area.setBackground(UIManager.getColor("Label.background"));
		area.setFont(UIManager.getFont("Label.font"));
		area.setBorder(UIManager.getBorder("Label.border"));
	}

	@Override
	public void handleLoad(File file) {
		this.controller.setFile(file);
		this.controller.load();
	}

	@Override
	public void loadCompleted() {
		SwingUtilities.invokeLater(() -> handlePopulate());

	}

	private void handlePopulate() {
		this.titleLabel.setText(this.titleLabel.getText() + this.controller.getAnswers().getTriviaName());
		createTreeNodes();
	}

	private void populateCardPanel(int nodeNum) {
		this.questionBorder.setTitle("Question " + (nodeNum + 1));
		Questions question = this.controller.getAnswers().getAnswerBank().getTriviaQuestion().get(nodeNum);
		this.questionArea.setText("Question: " + question.getQuestion() + "\n");
		this.answerArea.setText("Answer: " + question.getCorrectAnswer() + "\n");
		this.tableModel.setAnswers(question.getAnswers().getParticipant());
		repaint();

	}

	private void createTreeNodes() {
		int numberOfQuestions = this.controller.getAnswers().getAnswerBank().getTriviaQuestion().size();
		for (int i = 1; i <= numberOfQuestions; i++) {
			DefaultMutableTreeNode questionNode = new DefaultMutableTreeNode("Question " + i);
			this.treeModel.insertNodeInto(questionNode, top, top.getChildCount());
		}
		tree.expandPath(new TreePath(top.getPath()));
	}

	@Override
	public void loadFailed(String message) {
		SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(TriviaAnswerPanel.this,
				"An error was encountered while loading: " + message, "Error", JOptionPane.ERROR_MESSAGE));
	}

	@Override
	public void handleSave() {
		// Do nothing - This panel does not save!
	}

	@Override
	public void handleSaveAs(File file) {
		// Do nothing - This panel does not save!
	}

}
