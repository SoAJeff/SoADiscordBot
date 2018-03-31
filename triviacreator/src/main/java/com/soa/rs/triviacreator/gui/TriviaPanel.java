package com.soa.rs.triviacreator.gui;

import java.io.File;

import javax.swing.JPanel;

public interface TriviaPanel {

	public JPanel createPanel();

	public void handleLoad(File file);

	public void handleSave();

	public void handleSaveAs(File file);

}
