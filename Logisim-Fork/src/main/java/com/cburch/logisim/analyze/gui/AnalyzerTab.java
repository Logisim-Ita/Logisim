/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.analyze.gui;

import javax.swing.JPanel;

abstract class AnalyzerTab extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4864438824056672176L;

	abstract void localeChanged();

	abstract void updateTab();
}
