/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.data;

import java.awt.Window;

import javax.swing.JTextField;

import com.cburch.logisim.util.StringGetter;

public abstract class Attribute<V> {
	private String name;
	private StringGetter disp;

	public Attribute(String name, StringGetter disp) {
		this.name = name;
		this.disp = disp;
	}

	protected java.awt.Component getCellEditor(V value) {
		return new JTextField(toDisplayString(value));
	}

	public java.awt.Component getCellEditor(Window source, V value) {
		return getCellEditor(value);
	}

	public String getDisplayName() {
		return disp.get();
	}

	public String getName() {
		return name;
	}

	public abstract V parse(String value);

	public String toDisplayString(V value) {
		return value == null ? "" : value.toString();
	}

	public String toStandardString(V value) {
		return value.toString();
	}

	@Override
	public String toString() {
		return name;
	}
}
