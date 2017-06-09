/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.log;

import java.awt.LayoutManager;

import javax.swing.JPanel;

import com.cburch.logisim.proj.Project;

abstract class LogPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6666769652787945987L;
	private LogFrame logFrame;

	public LogPanel(LogFrame frame) {
		super();
		this.logFrame = frame;
	}

	public LogPanel(LogFrame frame, LayoutManager manager) {
		super(manager);
		this.logFrame = frame;
	}

	public abstract String getHelpText();

	LogFrame getLogFrame() {
		return logFrame;
	}

	Model getModel() {
		return logFrame.getModel();
	}

	Project getProject() {
		return logFrame.getProject();
	}

	Selection getSelection() {
		return logFrame.getModel().getSelection();
	}

	public abstract String getTitle();

	public abstract void localeChanged();

	public abstract void modelChanged(Model oldModel, Model newModel);
}
