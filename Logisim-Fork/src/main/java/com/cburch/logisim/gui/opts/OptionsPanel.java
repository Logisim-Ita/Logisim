/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.opts;

import java.awt.LayoutManager;

import javax.swing.JPanel;

import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.file.Options;
import com.cburch.logisim.proj.Project;

abstract class OptionsPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8712917074469641323L;
	private OptionsFrame optionsFrame;

	public OptionsPanel(OptionsFrame frame) {
		super();
		this.optionsFrame = frame;
	}

	public OptionsPanel(OptionsFrame frame, LayoutManager manager) {
		super(manager);
		this.optionsFrame = frame;
	}

	public abstract String getHelpText();

	LogisimFile getLogisimFile() {
		return optionsFrame.getLogisimFile();
	}

	Options getOptions() {
		return optionsFrame.getOptions();
	}

	OptionsFrame getOptionsFrame() {
		return optionsFrame;
	}

	Project getProject() {
		return optionsFrame.getProject();
	}

	public abstract String getTitle();

	public abstract void localeChanged();
}
