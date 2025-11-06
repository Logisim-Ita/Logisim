/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

public class LocaleManager {
	private static class LocaleGetter implements StringGetter {
		private LocaleManager source;
		private String key;

		LocaleGetter(LocaleManager source, String key) {
			this.source = source;
			this.key = key;
		}

		@Override
		public String get() {
			return source.get(key);
		}

		@Override
		public String toString() {
			return get();
		}
	}

	// static members
	private static final String SETTINGS_NAME = "settings.properties";

	private static ArrayList<LocaleManager> managers = new ArrayList<LocaleManager>();

	private static ArrayList<LocaleListener> listeners = new ArrayList<LocaleListener>();
	private static boolean replaceAccents = false;
	private static HashMap<Character, String> repl = null;
	private static Locale curLocale = null;

	public static void addLocaleListener(LocaleListener l) {
		listeners.add(l);
	}

	public static boolean canReplaceAccents() {
		return fetchReplaceAccents() != null;
	}

	private static HashMap<Character, String> fetchReplaceAccents() {
		HashMap<Character, String> ret = null;
		String val;
		try {
			val = Strings.source.prop.getProperty("accentReplacements");
		} catch (MissingResourceException e) {
			return null;
		}
		StringTokenizer toks = new StringTokenizer(val, "/");
		while (toks.hasMoreTokens()) {
			String tok = toks.nextToken().trim();
			char c = '\0';
			String s = null;
			if (tok.length() == 1) {
				c = tok.charAt(0);
				s = "";
			} else if (tok.length() >= 2 && tok.charAt(1) == ' ') {
				c = tok.charAt(0);
				s = tok.substring(2).trim();
			}
			if (s != null) {
				if (ret == null)
					ret = new HashMap<Character, String>();
				ret.put(Character.valueOf(c), s);
			}
		}
		return ret;
	}

	private static void fireLocaleChanged() {
		for (LocaleListener l : listeners) {
			l.localeChanged();
		}
	}

	public static Locale getLocale() {
		Locale ret = curLocale;
		if (ret == null) {
			ret = Locale.getDefault();
			curLocale = ret;
		}
		return ret;
	}

	public static void removeLocaleListener(LocaleListener l) {
		listeners.remove(l);
	}

	private static String replaceAccents(String src, HashMap<Character, String> repl) {
		// find first non-standard character - so we can avoid the
		// replacement process if possible
		int i = 0;
		int n = src.length();
		for (; i < n; i++) {
			char ci = src.charAt(i);
			if (ci < 32 || ci >= 127)
				break;
		}
		if (i == n)
			return src;

		// ok, we'll have to consider replacing accents
		char[] cs = src.toCharArray();
		StringBuilder ret = new StringBuilder(src.substring(0, i));
		for (int j = i; j < cs.length; j++) {
			char cj = cs[j];
			if (cj < 32 || cj >= 127) {
				String out = repl.get(Character.valueOf(cj));
				if (out != null) {
					ret.append(out);
				} else {
					ret.append(cj);
				}
			} else {
				ret.append(cj);
			}
		}
		return ret.toString();
	}

	public static void setLocale(Locale loc) {
		Locale cur = getLocale();
		if (!loc.equals(cur)) {
			Locale[] opts = Strings.getLocaleManager().getLocaleOptions();
			Locale select = null;
			Locale backup = null;
			String locLang = loc.getLanguage();
			for (Locale opt : opts) {
				if (select == null && opt.equals(loc)) {
					select = opt;
				}
				if (backup == null && opt.getLanguage().equals(locLang)) {
					backup = opt;
				}
			}
			if (select == null) {
				if (backup == null) {
					select = new Locale("en");
				} else {
					select = backup;
				}
			}

			curLocale = select;
			Locale.setDefault(select);
			for (LocaleManager man : managers) {
				man.loadDefault();
			}
			repl = replaceAccents ? fetchReplaceAccents() : null;
			fireLocaleChanged();
		}
	}

	public static void setReplaceAccents(boolean value) {
		HashMap<Character, String> newRepl = value ? fetchReplaceAccents() : null;
		replaceAccents = value;
		repl = newRepl;
		fireLocaleChanged();
	}

	// instance members
	private String dir_name;
	private String file_start;
	private Properties settin = null;														/**/
	private Properties prop = null;															/**/

	private Properties dflt_locale = null;													//default locale

	public LocaleManager(String dir_name, String file_start) {
		this.dir_name = dir_name;
		this.file_start = file_start + ".properties";
		loadDefault();
		managers.add(this);
	}

	public JComponent createLocaleSelector() {
		Locale[] locales = getLocaleOptions();
		if (locales == null || locales.length == 0) {
			Locale cur = getLocale();
			if (cur == null)
				cur = new Locale("en");
			locales = new Locale[] { cur };
		}
		return new JScrollPane(new LocaleSelector(locales));
	}

	public String get(String key) {
		String ret;
		
		if (prop.containsKey(key)) {
			ret = prop.getProperty(key);
		} else {
			if (dflt_locale == null) {
				dflt_locale = loadProperties(dir_name + "/en/" + file_start);	//set en properties in default
			}
			if (dflt_locale.containsKey(key)) {
				ret = dflt_locale.getProperty(key);
			} else {
				ret = key;
			}
		}
		HashMap<Character, String> repl = LocaleManager.repl;
		if (repl != null)
			ret = replaceAccents(ret, repl);
		
		return ret;
	}

	public Locale[] getLocaleOptions() {
		String locs = null;
		try {
			if (settin != null)
				locs = settin.getProperty("locales");
		} catch (MissingResourceException e) {
		}
		if (locs == null)
			return new Locale[] {};

		ArrayList<Locale> retl = new ArrayList<Locale>();
		StringTokenizer toks = new StringTokenizer(locs);
		while (toks.hasMoreTokens()) {
			String f = toks.nextToken();
			String language;
			String country;
			if (f.length() >= 2) {
				language = f.substring(0, 2);
				country = (f.length() >= 5 ? f.substring(3, 5) : null);
			} else {
				language = null;
				country = null;
			}
			if (language != null) {
				Locale loc = country == null ? new Locale(language) : new Locale(language, country);
				retl.add(loc);
			}
		}

		return retl.toArray(new Locale[retl.size()]);
	}

	public StringGetter getter(String key) {
		return new LocaleGetter(this, key);
	}

	public StringGetter getter(String key, String arg) {
		return StringUtil.formatter(getter(key), arg);
	}

	public StringGetter getter(String key, StringGetter arg) {
		return StringUtil.formatter(getter(key), arg);
	}

	private void loadDefault() {
		if (settin == null) {
			settin = loadProperties(dir_name + "/" + SETTINGS_NAME);
		}

		try {
			loadLocale(Locale.getDefault());
			if (prop != null)
				return;
		} catch (java.util.MissingResourceException e) {
		}
		try {
			loadLocale(Locale.ENGLISH);
			if (prop != null)
				return;
		} catch (java.util.MissingResourceException e) {
		}
		Locale[] choices = getLocaleOptions();
		if (choices != null && choices.length > 0)
			loadLocale(choices[0]);
		if (prop != null)
			return;
		throw new RuntimeException("No locale bundles are available");
	}
	
	private Properties loadProperties(String dir) {
		Properties properties = new Properties();
		try (InputStreamReader input = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(dir), StandardCharsets.UTF_8) ) {
			properties.load(input);
		} catch (NullPointerException e) {
			properties = loadProperties(dir_name + "/en/" + file_start);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return properties;
	}

	private void loadLocale(Locale loc) {
		String bundleName = dir_name + "/" + loc.getLanguage() + "/" + file_start;
		prop = loadProperties(bundleName);
	}
}
