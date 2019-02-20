package com.soa.rs.triviacreator.gui.help;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class HelpViewerDialog extends JDialog {

	public HelpViewerDialog(JFrame frame, String titleOfPageToDisplay) {
		super(frame, "TriviaCreator Help", false);
		displayDialog(titleOfPageToDisplay);
	}

	public HelpViewerDialog(JDialog dialog, String titleOfPageToDisplay) {
		super(dialog, "TriviaCreator Help", true);
		displayDialog(titleOfPageToDisplay);
	}

	private void displayDialog(String titleOfPageToDisplay) {
//		this.add(HelpViewerFactory.getInstance().getPanelForDialog());
		if (titleOfPageToDisplay != null) {
//			HelpViewerFactory.getInstance().getPanelForDialog().setDisplayedPageByTitle(titleOfPageToDisplay);
		}
		this.setPreferredSize(new Dimension(800, 600));
		this.pack();
		this.setVisible(true);
	}

}
