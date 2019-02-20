package com.soa.rs.triviacreator.gui.create;

import java.awt.BorderLayout;
import java.awt.Dimension;
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

import com.soa.rs.triviacreator.gui.help.HelpViewerDialog;
import com.soa.rs.triviacreator.util.DiscordType;
import com.soa.rs.triviacreator.util.InvalidTriviaConfigurationException;
import com.soa.rs.triviacreator.util.StringToDiscordPair;
import com.soa.rs.triviacreator.util.TriviaConfigValidator;

public class CreateDiscordPairDialog {

	private JTextField nameField;
	private JTextField idField;
	private StringToDiscordPair pair = null;
	private DiscordType type;
	private JDialog dialog;

	public CreateDiscordPairDialog(JFrame frame, String title, DiscordType type) {
		this.dialog = new JDialog(frame, title, true);
		this.type = type;
		JPanel panel = createPanel();
		this.dialog.add(panel);
		this.dialog.pack();
		this.dialog.setLocationRelativeTo(frame);
		this.dialog.setVisible(true);
	}

	private JPanel createPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(dataEntryPanel(), BorderLayout.NORTH);
		panel.add(buttonPanel(), BorderLayout.SOUTH);
		return panel;
	}

	private JPanel dataEntryPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = gbc.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(5, 5, 5, 5);

		JLabel nameLabel = new JLabel(this.type.getType() + " Name: ");
		gbc.weightx = 3;
		panel.add(nameLabel, gbc);

		gbc.gridx++;
		gbc.weightx = 10;
		this.nameField = new JTextField(20);
		panel.add(this.nameField, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 3;
		JLabel idLabel = new JLabel(this.type.getType() + "'s Discord ID: ");
		panel.add(idLabel, gbc);

		gbc.gridx++;
		gbc.weightx = 10;
		this.idField = new JTextField(20);
		panel.add(this.idField, gbc);

		gbc.gridx++;
		gbc.weightx = 1;
		JButton helpButton = new JButton("?");
		//Set the height of the button to be identical to the height of the TextField
		helpButton.setPreferredSize(
				new Dimension(helpButton.getPreferredSize().width, idField.getPreferredSize().height));
		helpButton.addActionListener(e -> {
			new HelpViewerDialog(dialog, null);
		});
		panel.add(helpButton, gbc);

		return panel;
	}

	private JPanel buttonPanel() {
		JPanel panel = new JPanel();
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (nameField.getText().isEmpty()) {
					JOptionPane.showMessageDialog(dialog, "A " + type.getType() + " name is required.",
							"Error validating information", JOptionPane.ERROR_MESSAGE);
				}
				try {
					TriviaConfigValidator.validateServerOrChannelId(idField.getText(), type);
					pair = new StringToDiscordPair(nameField.getText(), idField.getText());
					dialog.dispose();
				} catch (InvalidTriviaConfigurationException ex) {
					JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error validating information",
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

	public StringToDiscordPair getPair() {
		return this.pair;
	}
}
