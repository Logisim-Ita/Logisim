/* Copyright (c) 2011, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.generic;

public interface AttrTableModel {
	public void addAttrTableModelListener(AttrTableModelListener listener);

	public AttrTableModelRow getRow(int rowIndex);

	public int getRowCount();

	public String getTitle();

	public void removeAttrTableModelListener(AttrTableModelListener listener);
}
