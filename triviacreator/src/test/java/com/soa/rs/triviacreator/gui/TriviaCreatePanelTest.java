package com.soa.rs.triviacreator.gui;

import com.soa.rs.triviacreator.gui.create.DefaultTriviaCreateController;
import com.soa.rs.triviacreator.gui.create.DefaultTriviaCreateModel;
import com.soa.rs.triviacreator.gui.create.TriviaCreateController;
import com.soa.rs.triviacreator.gui.create.TriviaCreateModel;
import com.soa.rs.triviacreator.gui.create.TriviaCreatePanel;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class TriviaCreatePanelTest
{
	public static void main(String [] args)
	{
		TriviaCreateModel model = new DefaultTriviaCreateModel();
		TriviaCreateController controller  = new DefaultTriviaCreateController(model);
		TriviaCreatePanel panel = new TriviaCreatePanel(controller);
		JFrame frame = new JFrame();
		frame.add(panel.createPanel());
		frame.pack();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}