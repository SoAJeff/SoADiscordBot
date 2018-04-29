package com.soa.rs.triviacreator.gui.welcome;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.soa.rs.triviacreator.gui.MenuListener;
import com.soa.rs.triviacreator.util.FilePromptUtility;
import com.soa.rs.triviacreator.util.PanelType;

public class WelcomePanel extends JPanel {

	private MenuListener listener;
	private FilePromptUtility filePromptUtility = new FilePromptUtility();

	public WelcomePanel(MenuListener listener) {
		this.listener = listener;
	}

	public JPanel createPanel() {
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = gbc.CENTER;
		gbc.fill = gbc.REMAINDER;
		gbc.gridx = 0;
		gbc.gridy = 0;

		JLabel welcomeLabel = new JLabel("Welcome to Trivia Creator!");
		welcomeLabel.setFont(new Font("Serif", Font.PLAIN, 32));
		add(welcomeLabel, gbc);
		gbc.gridy++;

		JLabel textLabel = new JLabel("Select one of the following options to get started:");
		add(textLabel, gbc);
		gbc.gridy++;

		JButton newConfig = new JButton("Begin New Trivia Configuration");
		newConfig.addActionListener((e -> listener.createNewTabbedPanel(PanelType.TRIVIA_CREATE, null)));
		add(newConfig, gbc);
		gbc.gridy++;

		JButton loadConfig = new JButton("Load an Existing Configuration or Answer File");
		loadConfig.addActionListener(e -> {
			File loadFile = filePromptUtility.promptForFile("Open file...", true);
			if (loadFile != null)
				listener.loadFile(loadFile);
		});
		add(loadConfig, gbc);

		return this;
	}

}
