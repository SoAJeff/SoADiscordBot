package com.soa.rs.triviacreator.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

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

	public CreatorMenu(MenuListener listener) {
		this.listener = listener;
		this.add(createFileMenu());
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
				File loadFile = promptForFile("Open file...", true);
				if (loadFile != null)
					listener.loadFile(loadFile);

			}
		});

		this.closeTabConfigButton = new JMenuItem("Close tab");
		this.closeTabConfigButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				listener.closeTab();
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
				File saveFile = promptForFile("Save file...", false);
				if (saveFile != null)
					listener.saveAsFile(saveFile);
			}
		});

		this.exitButton = new JMenuItem("Exit");
		this.exitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int confirm = JOptionPane
						.showConfirmDialog(null, "Are you sure you want to quit?  Ensure all files have been saved.",
								"Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (confirm == JOptionPane.YES_OPTION)
					System.exit(0);

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

	private File promptForFile(String title, boolean load) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new FileNameExtensionFilter("XML file", "xml"));
		int returnValue;
		if (load)
			returnValue = chooser.showOpenDialog(null);
		else
			returnValue = chooser.showSaveDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			String filepath = chooser.getSelectedFile().getAbsolutePath();
			if (!filepath.endsWith(".xml")) {
				filepath = filepath + ".xml";
			}
			return new File(filepath);
		} else
			return null;
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
