package com.soa.rs.triviacreator.util;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FilePromptUtility {

	public File promptForFile(String title, boolean load) {
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
}
