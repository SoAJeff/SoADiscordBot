package com.soa.rs.triviacreator.gui.help;

//import com.github.soajeff.helpviewer.gui.DefaultHelpViewerController;
//import com.github.soajeff.helpviewer.gui.DefaultHelpViewerModel;
//import com.github.soajeff.helpviewer.gui.HelpViewerController;
//import com.github.soajeff.helpviewer.gui.HelpViewerModel;
//import com.github.soajeff.helpviewer.gui.HelpViewerPanel;

public class HelpViewerFactory {
	private static HelpViewerFactory ourInstance = new HelpViewerFactory();
	private boolean initialized = false;
//	private HelpViewerPanel helpViewerPanel;
//	private HelpViewerModel helpViewerModel;

	public static HelpViewerFactory getInstance() {
		return ourInstance;
	}

	private HelpViewerFactory() {
	}

	private void initializeHelpViewer()
	{
		if(!initialized)
		{
//			helpViewerModel = new DefaultHelpViewerModel();
//			//Generate Beans here
//
//			HelpViewerController controller = new DefaultHelpViewerController(helpViewerModel);
//			helpViewerPanel = new HelpViewerPanel(controller);
//			helpViewerPanel.initialize();
			initialized = true;
		}
	}

//	public HelpViewerPanel getPanelForDialog()
//	{
//		if(!initialized)
//			initializeHelpViewer();
//		return helpViewerPanel;
//	}
}
