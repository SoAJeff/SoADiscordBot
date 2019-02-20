package com.soa.rs.triviacreator.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import com.soa.rs.triviacreator.gui.help.HelpViewerDialog;
import com.soa.rs.triviacreator.util.FilePromptUtility;
import com.soa.rs.triviacreator.util.PanelType;

public class CreatorMenu extends JMenuBar {

	private MenuListener listener;
	private JMenu fileMenu;
	private JMenuItem newConfigButton;
	private JMenuItem loadConfigButton;
	private JMenuItem closeTabConfigButton;
	private JMenuItem saveConfigButton;
	private JMenuItem saveAsConfigButton;
	private JMenuItem exitButton;
	private JMenu helpMenu;
	private JMenuItem helpItem;
	private FilePromptUtility filePromptUtility;

	public CreatorMenu(MenuListener listener) {
		this.listener = listener;
		this.filePromptUtility = new FilePromptUtility();
		this.add(createFileMenu());
		this.add(createHelpMenu());

	}

	private JMenu createFileMenu() {
		this.fileMenu = new JMenu("File");
		this.newConfigButton = new JMenuItem("New Trivia Configuration");
		this.newConfigButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				listener.createNewTabbedPanel(PanelType.TRIVIA_CREATE, null);
			}
		});

		this.loadConfigButton = new JMenuItem("Open");
		this.loadConfigButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				File loadFile = filePromptUtility.promptForFile("Open file...", true);
				if (loadFile != null)
					listener.loadFile(loadFile);

			}
		});

		this.closeTabConfigButton = new JMenuItem("Close tab");
		this.closeTabConfigButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				listener.handleCloseTab();
			}
		});

		this.saveConfigButton = new JMenuItem("Save");
		this.saveConfigButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				listener.saveFile();
			}
		});

		this.saveAsConfigButton = new JMenuItem("Save As...");
		this.saveAsConfigButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				File saveFile = filePromptUtility.promptForFile("Save file...", false);
				if (saveFile != null)
					listener.saveAsFile(saveFile);
			}
		});

		this.exitButton = new JMenuItem("Exit");
		this.exitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				listener.askToCloseApplication();
			}
		});

		this.fileMenu.add(this.newConfigButton);
		this.fileMenu.add(this.loadConfigButton);
		this.fileMenu.addSeparator();
		this.fileMenu.add(this.closeTabConfigButton);
		this.fileMenu.addSeparator();
		this.fileMenu.add(saveConfigButton);
		this.fileMenu.add(this.saveAsConfigButton);
		this.fileMenu.addSeparator();
		this.fileMenu.add(this.exitButton);
		return this.fileMenu;
	}

	private JMenu createHelpMenu()
	{
		this.helpMenu = new JMenu("Help");

		this.helpItem = new JMenuItem("Open Help");
		this.helpItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new HelpViewerDialog(new JFrame(), null);
			}
		});

		this.helpMenu.add(this.helpItem);

		return this.helpMenu;
	}

	public void toggleSaveOptionsEnabled(boolean enabled) {
		SwingUtilities.invokeLater(() -> {
			if (enabled) {
				this.saveConfigButton.setEnabled(true);
				this.saveAsConfigButton.setEnabled(true);
			} else {
				this.saveConfigButton.setEnabled(false);
				this.saveAsConfigButton.setEnabled(false);
			}
		});
	}

	public void toggleCloseOptionEnabled(boolean enabled) {
		SwingUtilities.invokeLater(() -> {

			if (enabled) {
				this.closeTabConfigButton.setEnabled(true);
			} else {
				this.closeTabConfigButton.setEnabled(false);
			}
		});

	}

}
