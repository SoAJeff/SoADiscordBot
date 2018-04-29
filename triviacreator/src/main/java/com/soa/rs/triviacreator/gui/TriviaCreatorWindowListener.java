package com.soa.rs.triviacreator.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import com.soa.rs.triviacreator.gui.welcome.WelcomePanel;

public class TriviaCreatorWindowListener implements WindowListener {

	private JTabbedPane pane;

	public TriviaCreatorWindowListener(JTabbedPane pane) {
		this.pane = pane;
	}

	@Override
	public void windowOpened(WindowEvent e) {

	}

	@Override
	public void windowClosing(WindowEvent e) {
		checkTabsAndAskToClose();

	}

	public void checkTabsAndAskToClose() {
		//First, check if no tabs are open, or if there is only a single tab open and it is the welcome tab.  There is no data to be lost in these.
		if (pane.getTabCount() == 0 || (pane.getTabCount() == 1 && pane.getSelectedComponent() instanceof WelcomePanel))
			System.exit(0);
		else {
			//There is a possibility of there being data open, so confirm that all is saved
			int confirm = JOptionPane
					.showConfirmDialog(null, "Are you sure you want to quit?  Ensure all files have been saved.",
							"Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (confirm == JOptionPane.YES_OPTION)
				System.exit(0);
		}
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
