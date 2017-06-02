/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.toolbar;

import java.util.EventObject;

public class ToolbarModelEvent extends EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5329854608387181565L;

	public ToolbarModelEvent(ToolbarModel model) {
		super(model);
	}
}
