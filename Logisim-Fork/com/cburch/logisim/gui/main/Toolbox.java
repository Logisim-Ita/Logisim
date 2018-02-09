/* Copyright (c) 2011, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.cburch.draw.toolbar.Toolbar;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.Tool;

class Toolbox extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5713124867071121331L;
	private ProjectExplorer toolbox;

	Toolbox(Frame frame, Project proj, MenuListener menu) {
		super(new BorderLayout());

		ToolboxToolbarModel toolbarModel = new ToolboxToolbarModel(frame, menu);
		Toolbar toolbar = new Toolbar(toolbarModel);
		add(toolbar, BorderLayout.NORTH);

		toolbox = new ProjectExplorer(proj);
		toolbox.setListener(new ToolboxManip(proj, toolbox));
		add(new JScrollPane(toolbox), BorderLayout.CENTER);
	}

	void setHaloedTool(Tool value) {
		toolbox.setHaloedTool(value);
	}
}
