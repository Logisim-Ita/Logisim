/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.proj;

public abstract class Action {
	public Action append(Action other) {
		return new JoinedAction(this, other);
	}

	public abstract void doIt(Project proj);

	public abstract String getName();

	public boolean isModification() {
		return true;
	}

	public boolean shouldAppendTo(Action other) {
		return false;
	}

	public abstract void undo(Project proj);
}
