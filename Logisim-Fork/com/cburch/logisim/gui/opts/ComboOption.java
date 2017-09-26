/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.opts;

import javax.swing.JComboBox;

import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.util.StringGetter;

class ComboOption {
	static void setSelected(JComboBox<?> combo, Object value) {
		for (int i = combo.getItemCount() - 1; i >= 0; i--) {
			ComboOption opt = (ComboOption) combo.getItemAt(i);
			if (opt.getValue().equals(value)) {
				combo.setSelectedItem(opt);
				return;
			}
		}
		combo.setSelectedItem(combo.getItemAt(0));
	}

	private Object value;

	private StringGetter getter;

	ComboOption(AttributeOption value) {
		this.value = value;
		this.getter = null;
	}

	ComboOption(String value, StringGetter getter) {
		this.value = value;
		this.getter = getter;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		if (getter != null)
			return getter.get();
		if (value instanceof AttributeOption)
			return ((AttributeOption) value).toDisplayString();
		return "???";
	}

}
