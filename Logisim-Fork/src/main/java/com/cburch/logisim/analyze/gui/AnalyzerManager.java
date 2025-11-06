/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.analyze.gui;

import javax.swing.JFrame;

import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;
import com.cburch.logisim.util.WindowMenuItemManager;

public class AnalyzerManager extends WindowMenuItemManager implements LocaleListener {
	private static Analyzer analysisWindow = null;

	private static AnalyzerManager analysisManager = null;

	public static Analyzer getAnalyzer() {
		if (analysisWindow == null) {
			analysisWindow = new Analyzer();
			analysisWindow.pack();
			analysisWindow.setLocationRelativeTo(null);
			if (analysisManager != null)
				analysisManager.frameOpened(analysisWindow);
		}
		return analysisWindow;
	}

	public static void initialize() {
		analysisManager = new AnalyzerManager();
	}

	private AnalyzerManager() {
		super(Strings.get("analyzerWindowTitle"), true);
		LocaleManager.addLocaleListener(this);
	}

	@Override
	public JFrame getJFrame(boolean create) {
		if (create) {
			return getAnalyzer();
		} else {
			return analysisWindow;
		}
	}

	@Override
	public void localeChanged() {
		setText(Strings.get("analyzerWindowTitle"));
	}
}
