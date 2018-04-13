package com.soa.rs.triviacreator.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JOptionPane;

public class TriviaCreatorWindowListener implements WindowListener {

	@Override
	public void windowOpened(WindowEvent e) {

	}

	@Override
	public void windowClosing(WindowEvent e) {
		int confirm = JOptionPane
				.showConfirmDialog(null, "Are you sure you want to quit?  Ensure all files have been saved.",
						"Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (confirm == JOptionPane.YES_OPTION)
			System.exit(0);
	}

	@Override
	public void windowClosed(WindowEvent e) {

	}

	@Override
	public void windowIconified(WindowEvent e) {

	}

	@Override
	public void windowDeiconified(WindowEvent e) {

	}

	@Override
	public void windowActivated(WindowEvent e) {

	}

	@Override
	public void windowDeactivated(WindowEvent e) {

	}
}
