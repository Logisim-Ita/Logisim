/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JRadioButtonMenuItem;

class WindowMenuItem extends JRadioButtonMenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3278632701872758005L;
	private WindowMenuItemManager manager;

	WindowMenuItem(WindowMenuItemManager manager) {
		this.manager = manager;
		setText(manager.getText());
		setSelected(WindowMenuManager.getCurrentManager() == manager);
	}

	public void actionPerformed(ActionEvent event) {
		JFrame frame = getJFrame();
		frame.setExtendedState(Frame.NORMAL);
		frame.setVisible(true);
		frame.toFront();
	}

	public JFrame getJFrame() {
		return manager.getJFrame(true);
	}
}
