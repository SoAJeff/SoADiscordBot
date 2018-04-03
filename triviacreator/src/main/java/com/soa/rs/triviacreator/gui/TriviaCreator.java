package com.soa.rs.triviacreator.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

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
import com.soa.rs.triviacreator.util.ConfigFileTypeValidator;
import com.soa.rs.triviacreator.util.PanelType;

public class TriviaCreator implements MenuListener {

	private JFrame frame;
	private CreatorMenu menu;
	private JTabbedPane tabbedPane;

	public static void main(String[] args) {
		TriviaCreator creator = new TriviaCreator();
		creator.initialize();
	}

	public void initialize() {
		this.frame = new JFrame();
		menu = new CreatorMenu(this);
		this.frame.setJMenuBar(menu);
		JPanel tabPanel = new JPanel();
		tabPanel.setLayout(new GridLayout(1, 1));
		tabbedPane = new JTabbedPane();
		tabPanel.add(tabbedPane);
		frame.add(tabPanel);
		frame.setTitle("Trivia Creator");
		frame.pack();
		frame.setMinimumSize(new Dimension(800, 800));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
			// something...
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

	}

	@Override
	public void closeTab() {
		try {
			tabbedPane.remove(tabbedPane.getSelectedIndex());
		} catch (IndexOutOfBoundsException e) {
		}

	}

}
