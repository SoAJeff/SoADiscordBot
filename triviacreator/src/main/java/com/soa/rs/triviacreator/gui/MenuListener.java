package com.soa.rs.triviacreator.gui;

import java.io.File;

import com.soa.rs.triviacreator.util.PanelType;

public interface MenuListener {
	
	public void loadFile(File file);
	
	public void saveFile();
	
	public void saveAsFile(File file);
	
	public void createNewTabbedPanel(PanelType type, File file);
	
	public void closeTab();

	public void askToCloseApplication();
	
}
