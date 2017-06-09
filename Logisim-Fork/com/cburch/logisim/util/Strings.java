/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

class Strings {
	static LocaleManager source = new LocaleManager("resources/logisim", "util");

	public static String get(String key) {
		return source.get(key);
	}

	public static LocaleManager getLocaleManager() {
		return source;
	}

	public static StringGetter getter(String key) {
		return source.getter(key);
	}
}
