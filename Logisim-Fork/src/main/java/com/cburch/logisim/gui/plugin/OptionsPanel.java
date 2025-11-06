/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.plugin;

import java.awt.LayoutManager;

import javax.swing.JPanel;

abstract class OptionsPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2637847947425197542L;
	private PluginFrame optionsFrame;

	public OptionsPanel(PluginFrame frame) {
		super();
		this.optionsFrame = frame;
	}

	public OptionsPanel(PluginFrame frame, LayoutManager manager) {
		super(manager);
		this.optionsFrame = frame;
	}

	public abstract String getHelpText();

	PluginFrame getPreferencesFrame() {
		return optionsFrame;
	}

	public abstract String getTitle();
}
