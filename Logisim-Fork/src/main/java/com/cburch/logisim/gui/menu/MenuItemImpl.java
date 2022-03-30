/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

class MenuItemImpl extends JMenuItem implements MenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1045330005196255936L;
	private MenuItemHelper helper;

	public MenuItemImpl(Menu menu, LogisimMenuItem menuItem) {
		helper = new MenuItemHelper(this, menu, menuItem);
		super.addActionListener(helper);
		setEnabled(true);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		helper.actionPerformed(event);
	}

	@Override
	public void addActionListener(ActionListener l) {
		helper.addActionListener(l);
	}

	@Override
	public boolean hasListeners() {
		return helper.hasListeners();
	}

	@Override
	public void removeActionListener(ActionListener l) {
		helper.removeActionListener(l);
	}

	@Override
	public void setEnabled(boolean value) {
		helper.setEnabled(value);
		super.setEnabled(value && helper.hasListeners());
	}
}