/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

interface MenuItem {
	public void actionPerformed(ActionEvent event);

	public void addActionListener(ActionListener l);

	boolean hasListeners();

	public boolean isEnabled();

	public void removeActionListener(ActionListener l);

	public void setEnabled(boolean value);
}
