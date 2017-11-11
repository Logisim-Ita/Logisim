/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.tools;

import com.cburch.logisim.util.LocaleManager;
import com.cburch.logisim.util.StringUtil;

class Strings {
	private static LocaleManager source = new LocaleManager("resources/logisim", "draw");

	public static String getTooltip(String key) {
		return StringUtil.format(source.get("actionAdd"), source.get(key));
	}
}
