package com.soa.rs.triviacreator.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.soa.rs.triviacreator.gui.answer.DefaultTriviaAnswerController;
import com.soa.rs.triviacreator.gui.answer.DefaultTriviaAnswerModel;
import com.soa.rs.triviacreator.gui.answer.TriviaAnswerController;
import com.soa.rs.triviacreator.gui.answer.TriviaAnswerModel;
import com.soa.rs.triviacreator.gui.answer.TriviaAnswerPanel;
import com.soa.rs.triviacreator.gui.create.DefaultTriviaCreateController;
import com.soa.rs.triviacreator.gui.create.DefaultTriviaCreateModel;
import com.soa.rs.triviacreator.gui.create.TriviaCreateController;
import com.soa.rs.triviacreator.gui.create.TriviaCreateModel;
import com.soa.rs.triviacreator.gui.create.TriviaCreatePanel;
import com.soa.rs.triviacreator.gui.welcome.WelcomePanel;
import com.soa.rs.triviacreator.util.ConfigFileTypeValidator;
import com.soa.rs.triviacreator.util.PanelType;

public class TriviaCreator implements MenuListener {

	private JFrame frame;
	private CreatorMenu menu;
	private JTabbedPane tabbedPane;
	private TriviaCreatorWindowListener windowListener;

	public static void main(String[] args) {
		TriviaCreator creator = new TriviaCreator();
		creator.initialize();
	}

	private void initialize() {
		this.frame = new JFrame();
		menu = new CreatorMenu(this);
		menu.toggleSaveOptionsEnabled(false);
		this.frame.setJMenuBar(menu);
		JPanel tabPanel = new JPanel();
		tabPanel.setLayout(new GridLayout(1, 1));
		tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(e -> handleSelectedTabChanged());
		tabPanel.add(tabbedPane);
		WelcomePanel welcomePanel = new WelcomePanel(this);
		tabbedPane.add("Welcome", welcomePanel.createPanel());
		frame.add(tabPanel);
		frame.setTitle("Trivia Creator");
		frame.pack();
		frame.setMinimumSize(new Dimension(800, 800));
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		windowListener = new TriviaCreatorWindowListener(tabbedPane);
		frame.addWindowListener(windowListener);
		frame.setVisible(true);

	}

	@Override
	public void loadFile(File file) {
		try {
			if (ConfigFileTypeValidator.getPanelTypeForFile(file) == PanelType.TRIVIA_CREATE)
				createNewTabbedPanel(PanelType.TRIVIA_CREATE, file);
			else
				createNewTabbedPanel(PanelType.TRIVIA_ANSWER, file);
		} catch (Exception e) {
			notifyLoadError(e);
		}

	}

	@Override
	public void saveFile() {
		TriviaPanel component = (TriviaPanel) this.tabbedPane.getSelectedComponent();
		if (component instanceof TriviaCreatePanel) {
			component.handleSave();
		}
	}

	@Override
	public void saveAsFile(File file) {
		TriviaPanel component = (TriviaPanel) this.tabbedPane.getSelectedComponent();
		if (component instanceof TriviaCreatePanel) {
			component.handleSaveAs(file);
		}
	}

	@Override
	public void createNewTabbedPanel(PanelType type, File file) {
		String tabName = "New Config";
		if (type == PanelType.TRIVIA_CREATE) {
			TriviaCreateModel model = new DefaultTriviaCreateModel();
			TriviaCreateController controller = new DefaultTriviaCreateController(model);
			TriviaCreatePanel panel = new TriviaCreatePanel(controller);
			if (file != null)
				tabName = file.getName();
			tabbedPane.addTab(tabName, panel.createPanel());
			if (file != null)
				panel.handleLoad(file);

		} else if (type == PanelType.TRIVIA_ANSWER) {
			if (file != null) {
				TriviaAnswerModel model = new DefaultTriviaAnswerModel();
				TriviaAnswerController controller = new DefaultTriviaAnswerController(model);
				TriviaAnswerPanel panel = new TriviaAnswerPanel(controller);
				tabbedPane.addTab(file.getName(), panel.createPanel());
				panel.handleLoad(file);
			}
		}
		tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
		menu.toggleCloseOptionEnabled(true);

	}

	@Override
	public void handleCloseTab() {
		//Prompt to save if the tab is a configuration - we don't need to prompt on Welcome or Answers.
		if (tabbedPane.getSelectedComponent() instanceof TriviaCreatePanel) {
			int confirm = JOptionPane.showConfirmDialog(frame,
					"Are you sure you want to close this tab?  Ensure your configuration is saved before clicking Yes.",
					"Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (confirm == JOptionPane.YES_OPTION)
				closeTab();
		} else {
			closeTab();
		}
	}

	private void closeTab() {
		try {
			tabbedPane.remove(tabbedPane.getSelectedIndex());
		} catch (IndexOutOfBoundsException e) {
			notifyError("Unable to close tab, " + e.getMessage());
		} finally {
			if (tabbedPane.getTabCount() == 0) {
				menu.toggleCloseOptionEnabled(false);
				menu.toggleSaveOptionsEnabled(false);
			}

		}
	}

	@Override
	public void askToCloseApplication() {
		windowListener.checkTabsAndAskToClose();
	}

	private void handleSelectedTabChanged() {
		if (tabbedPane.getTabCount() > 0) {
			Component component = tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
			if (component instanceof TriviaCreatePanel) {
				menu.toggleSaveOptionsEnabled(true);
			} else {
				menu.toggleSaveOptionsEnabled(false);
			}
		}
	}

	private void notifyLoadError(Exception e) {
		SwingUtilities.invokeLater(() -> JOptionPane
				.showMessageDialog(frame, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
	}

	private void notifyError(String message) {
		SwingUtilities.invokeLater(() -> JOptionPane
				.showMessageDialog(frame, "An error has occurred: " + message, "Error", JOptionPane.ERROR_MESSAGE));
	}

}
