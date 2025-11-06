/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.prefs;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.cburch.logisim.Main;
import com.cburch.logisim.circuit.RadixOption;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.gui.start.Startup;
import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;
import com.cburch.logisim.util.PropertyChangeWeakSupport;

public class AppPreferences {
	//
	// LocalePreference
	//
	private static class LocalePreference extends PrefMonitorString {
		private static Locale findLocale(String lang) {
			Locale[] check;
			for (int set = 0; set < 2; set++) {
				if (set == 0)
					check = new Locale[] { Locale.getDefault(), Locale.ENGLISH };
				else
					check = Locale.getAvailableLocales();
				for (int i = 0; i < check.length; i++) {
					Locale loc = check[i];
					if (loc != null && loc.getLanguage().equals(lang)) {
						return loc;
					}
				}
			}
			return null;
		}

		public LocalePreference() {
			super("locale", "");

			String localeStr = this.get();
			if (localeStr != null && !localeStr.equals("")) {
				LocaleManager.setLocale(new Locale(localeStr));
			}
			LocaleManager.addLocaleListener(myListener);
			myListener.localeChanged();
		}

		@Override
		public void set(String value) {
			if (findLocale(value) != null) {
				super.set(value);
			}
		}
	}

	//
	// methods for accessing preferences
	//
	private static class MyListener implements PreferenceChangeListener, LocaleListener {
		@Override
		public void localeChanged() {
			Locale loc = LocaleManager.getLocale();
			String lang = loc.getLanguage();
			if (LOCALE != null) {
				LOCALE.set(lang);
			}
		}

		@Override
		public void preferenceChange(PreferenceChangeEvent event) {
			Preferences prefs = event.getNode();
			String prop = event.getKey();
			if (ACCENTS_REPLACE.getIdentifier().equals(prop)) {
				getPrefs();
				LocaleManager.setReplaceAccents(ACCENTS_REPLACE.getBoolean());
			} else if (prop.equals(TEMPLATE_TYPE)) {
				int oldValue = templateType;
				int value = prefs.getInt(TEMPLATE_TYPE, TEMPLATE_UNKNOWN);
				if (value != oldValue) {
					templateType = value;
					propertySupport.firePropertyChange(TEMPLATE, oldValue, value);
					propertySupport.firePropertyChange(TEMPLATE_TYPE, oldValue, value);
				}
			} else if (prop.equals(TEMPLATE_FILE)) {
				File oldValue = templateFile;
				File value = convertFile(prefs.get(TEMPLATE_FILE, null));
				if (value == null ? oldValue != null : !value.equals(oldValue)) {
					templateFile = value;
					if (templateType == TEMPLATE_CUSTOM) {
						customTemplate = null;
						propertySupport.firePropertyChange(TEMPLATE, oldValue, value);
					}
					propertySupport.firePropertyChange(TEMPLATE_FILE, oldValue, value);
				}
			} else if (prop.equals(LIBRARIES_FOLDER_PATH)) {
				File oldValue = LibrariesFolder;
				File value = convertFile(prefs.get(LIBRARIES_FOLDER_PATH, null));
				if (value == null ? oldValue != null : !value.equals(oldValue)) {
					LibrariesFolder = value;
					propertySupport.firePropertyChange(LIBRARIES_FOLDER_PATH, oldValue, value);
				}
			}
		}
	}

	// class variables for maintaining consistency between properties,
	// internal variables, and other classes
	private static Preferences prefs = null;

	private static MyListener myListener = null;
	private static PropertyChangeWeakSupport propertySupport = new PropertyChangeWeakSupport(AppPreferences.class);
	// Template preferences
	public static final int TEMPLATE_UNKNOWN = -1;
	public static final int TEMPLATE_EMPTY = 0;

	public static final int TEMPLATE_PLAIN = 1;
	public static final int TEMPLATE_CUSTOM = 2;
	public static final String TEMPLATE = "template";

	public static final String TEMPLATE_TYPE = "templateType";
	public static final String TEMPLATE_FILE = "templateFile";

	private static int templateType = TEMPLATE_PLAIN;
	private static File templateFile = null;
	private static Template plainTemplate = null;
	private static Template emptyTemplate = null;

	private static Template customTemplate = null;
	private static File customTemplateFile = null;
	// International preferences
	public static final String SHAPE_SHAPED = "shaped";

	public static final String SHAPE_RECTANGULAR = "rectangular";
	public static final String SHAPE_DIN40700 = "din40700";
	public static final PrefMonitor<String> GATE_SHAPE = create(new PrefMonitorStringOpts("gateShape",
			new String[] { SHAPE_SHAPED, SHAPE_RECTANGULAR, SHAPE_DIN40700 }, SHAPE_SHAPED));

	public static final PrefMonitor<String> LOCALE = create(new LocalePreference());
	public static final PrefMonitor<Boolean> ACCENTS_REPLACE = create(new PrefMonitorBoolean("accentsReplace", false));

	// Window preferences
	public static final String TOOLBAR_HIDDEN = "hidden";
	public static final String TOOLBAR_DOWN_MIDDLE = "downMiddle";

	public static final PrefMonitor<Boolean> SHOW_TICK_RATE = create(new PrefMonitorBoolean("showTickRate", true));
	public static final PrefMonitor<String> TOOLBAR_PLACEMENT = create(new PrefMonitorStringOpts(
			"toolbarPlacement", new String[] { Direction.NORTH.toString(), Direction.SOUTH.toString(),
					Direction.EAST.toString(), Direction.WEST.toString(), TOOLBAR_DOWN_MIDDLE, TOOLBAR_HIDDEN },
			Direction.NORTH.toString()));
	public static final PrefMonitor<String> REFRESH_RATE = create(
			new PrefMonitorStringOpts("windowRefreshRate", new String[] { "20", "30", "60", "120", "144" }, "60"));
	// Layout preferences
	public static final String ADD_AFTER_UNCHANGED = "unchanged";
	public static final String ADD_AFTER_EDIT = "edit";
	public static final PrefMonitor<Boolean> PRINTER_VIEW = create(new PrefMonitorBoolean("printerView", false));
	public static final PrefMonitor<Boolean> ATTRIBUTE_HALO = create(new PrefMonitorBoolean("attributeHalo", true));
	public static final PrefMonitor<Boolean> COMPONENT_TIPS = create(new PrefMonitorBoolean("componentTips", true));
	public static final PrefMonitor<Boolean> MOVE_KEEP_CONNECT = create(new PrefMonitorBoolean("keepConnected", true));
	public static final PrefMonitor<Boolean> ADD_SHOW_GHOSTS = create(new PrefMonitorBoolean("showGhosts", true));
	public static final PrefMonitor<String> ADD_AFTER = create(new PrefMonitorStringOpts("afterAdd",
			new String[] { ADD_AFTER_EDIT, ADD_AFTER_UNCHANGED }, ADD_AFTER_EDIT));

	public static PrefMonitor<String> POKE_WIRE_RADIX1;

	public static PrefMonitor<String> POKE_WIRE_RADIX2;

	static {
		RadixOption[] radixOptions = RadixOption.OPTIONS;
		String[] radixStrings = new String[radixOptions.length];
		for (int i = 0; i < radixOptions.length; i++) {
			radixStrings[i] = radixOptions[i].getSaveString();
		}
		POKE_WIRE_RADIX1 = create(
				new PrefMonitorStringOpts("pokeRadix1", radixStrings, RadixOption.RADIX_2.getSaveString()));
		POKE_WIRE_RADIX2 = create(
				new PrefMonitorStringOpts("pokeRadix2", radixStrings, RadixOption.RADIX_10_UNSIGNED.getSaveString()));
	}

	// Experimental preferences
	public static final String ACCEL_DEFAULT = "default";
	public static final String ACCEL_NONE = "none";

	public static final String ACCEL_OPENGL = "opengl";

	public static final String ACCEL_D3D = "d3d";
	public static final PrefMonitor<String> GRAPHICS_ACCELERATION = create(
			new PrefMonitorStringOpts("graphicsAcceleration",
					new String[] { ACCEL_DEFAULT, ACCEL_NONE, ACCEL_OPENGL, ACCEL_D3D }, ACCEL_DEFAULT));
	// fork preferences
	public static final PrefMonitor<Boolean> ANTI_ALIASING = create(new PrefMonitorBoolean("AntiAliasing", true));
	public static final PrefMonitor<Boolean> FILL_COMPONENT_BACKGROUND = create(
			new PrefMonitorBoolean("FillComponentBackground", true));
	// public static final PrefMonitor<Boolean> SEND_DATA = create(new
	// PrefMonitorBoolean("SendLogisimUsageData", true));
	public static final PrefMonitor<Boolean> NEW_TOOLBAR = create(new PrefMonitorBoolean("UseSimpleToolbar", true));
	public static final PrefMonitor<Boolean> LOAD_LIBRARIES_FOLDER_AT_STARTUP = create(
			new PrefMonitorBoolean("LoadLibrariesAtStartup", false));
	public static final String LIBRARIES_FOLDER_PATH = "LibrariesFolderPath";
	private static File LibrariesFolder = null;
	public static final String ALWAYS = "Always";
	public static final String ASKME = "Ask Me";
	public static final String NO = "No";
	public static final PrefMonitor<String> AUTO_UPDATES = create(
			new PrefMonitorStringOpts("AutoUpdates", new String[] { ALWAYS, ASKME, NO }, ASKME));
	public static final String SYSTEM = UIManager.getSystemLookAndFeelClassName();
	public static final String NIMBUS = NimbusLookAndFeel.class.getName();
	public static final String METAL = MetalLookAndFeel.class.getName();
	public static final PrefMonitor<String> LOOK_AND_FEEL = create(
			new PrefMonitorStringOpts("lookAndFeel", new String[] { SYSTEM, NIMBUS, METAL }, SYSTEM));
    //Logisim Folder Preferences
    public static final String LOGISIM_FOLDER = "not_set";
	// hidden window preferences - not part of the preferences dialog, changes
	// to preference does not affect current windows, and the values are not
	// saved until the application is closed
	public static final String RECENT_PROJECTS = "recentProjects";
	private static final RecentProjects recentProjects = new RecentProjects();
	public static final PrefMonitor<Double> TICK_FREQUENCY = create(new PrefMonitorDouble("tickFrequency", 1.0));
	public static final PrefMonitor<Boolean> LAYOUT_SHOW_GRID = create(new PrefMonitorBoolean("layoutGrid", true));
	public static final PrefMonitor<Double> LAYOUT_ZOOM = create(new PrefMonitorDouble("layoutZoom", 1.0));
	public static final PrefMonitor<Boolean> APPEARANCE_SHOW_GRID = create(
			new PrefMonitorBoolean("appearanceGrid", true));
	public static final PrefMonitor<Double> APPEARANCE_ZOOM = create(new PrefMonitorDouble("appearanceZoom", 1.0));
	public static final PrefMonitor<Integer> WINDOW_STATE = create(new PrefMonitorInt("windowState", JFrame.NORMAL));
	public static final PrefMonitor<Integer> WINDOW_WIDTH = create(new PrefMonitorInt("windowWidth", 640));
	public static final PrefMonitor<Integer> WINDOW_HEIGHT = create(new PrefMonitorInt("windowHeight", 480));
	public static final PrefMonitor<String> WINDOW_LOCATION = create(new PrefMonitorString("windowLocation", "0,0"));
	public static final PrefMonitor<Double> WINDOW_MAIN_SPLIT = create(new PrefMonitorDouble("windowMainSplit", 0.25));

	public static final PrefMonitor<Double> WINDOW_LEFT_SPLIT = create(new PrefMonitorDouble("windowLeftSplit", 0.5));

	public static final PrefMonitor<String> DIALOG_DIRECTORY = create(new PrefMonitorString("dialogDirectory", ""));

	//
	// PropertyChangeSource methods
	//
	public static void addPropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}

	public static void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(propertyName, listener);
	}

	public static void clear() {
		Preferences p = getPrefs(true);
		try {
			p.clear();
		} catch (BackingStoreException e) {
		}
	}

	private static File convertFile(String fileName) {
		if (fileName == null || fileName.equals("")) {
			return null;
		} else {
			File file = new File(fileName);
			return file.canRead() ? file : null;
		}
	}

	private static <E> PrefMonitor<E> create(PrefMonitor<E> monitor) {
		return monitor;
	}

	static void firePropertyChange(String property, boolean oldVal, boolean newVal) {
		propertySupport.firePropertyChange(property, oldVal, newVal);
	}

	static void firePropertyChange(String property, Object oldVal, Object newVal) {
		propertySupport.firePropertyChange(property, oldVal, newVal);
	}

	private static Template getCustomTemplate() {
		File toRead = templateFile;
		if (customTemplateFile == null || !(customTemplateFile.equals(toRead))) {
			if (toRead == null) {
				customTemplate = null;
				customTemplateFile = null;
			} else {
				FileInputStream reader = null;
				try {
					reader = new FileInputStream(toRead);
					customTemplate = Template.create(reader);
					customTemplateFile = templateFile;
				} catch (Throwable t) {
					setTemplateFile(null);
					customTemplate = null;
					customTemplateFile = null;
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException e) {
						}
					}
				}
			}
		}
		return customTemplate == null ? getPlainTemplate() : customTemplate;
	}

	public static Template getEmptyTemplate() {
		if (emptyTemplate == null)
			emptyTemplate = Template.createEmpty();
		return emptyTemplate;
	}

	public static File getLibrariesFolder() {
		getPrefs();
		return LibrariesFolder;
	}

	private static Template getPlainTemplate() {
		if (plainTemplate == null) {
			ClassLoader ld = Startup.class.getClassLoader();
			InputStream in = ld.getResourceAsStream("resources/logisim/default.templ");
			if (in == null) {
				plainTemplate = getEmptyTemplate();
			} else {
				try {
					try {
						plainTemplate = Template.create(in);
					} finally {
						in.close();
					}
				} catch (Throwable e) {
					plainTemplate = getEmptyTemplate();
				}
			}
		}
		return plainTemplate;
	}

	static Preferences getPrefs() {
		return getPrefs(false);
	}

	private static Preferences getPrefs(boolean shouldClear) {
		if (prefs == null) {
			synchronized (AppPreferences.class) {
				if (prefs == null) {
					Preferences p = Preferences.userNodeForPackage(Main.class);
					if (shouldClear) {
						try {
							p.clear();
						} catch (BackingStoreException e) {
						}
					}
					myListener = new MyListener();
					p.addPreferenceChangeListener(myListener);
					prefs = p;

					setTemplateFile(convertFile(p.get(TEMPLATE_FILE, null)));
					setLibrariesFolder(convertFile(p.get(LIBRARIES_FOLDER_PATH, null)));
					setTemplateType(p.getInt(TEMPLATE_TYPE, TEMPLATE_PLAIN));
				}
			}
		}
		return prefs;
	}

	//
	// recent projects
	//
	public static List<File> getRecentFiles() {
		return recentProjects.getRecentFiles();
	}

	//
	// template methods
	//
	public static Template getTemplate() {
		getPrefs();
		switch (templateType) {
		case TEMPLATE_PLAIN:
			return getPlainTemplate();
		case TEMPLATE_EMPTY:
			return getEmptyTemplate();
		case TEMPLATE_CUSTOM:
			return getCustomTemplate();
		default:
			return getPlainTemplate();
		}
	}

	public static File getTemplateFile() {
		getPrefs();
		return templateFile;
	}

	//
	// accessor methods
	//
	public static int getTemplateType() {
		getPrefs();
		int ret = templateType;
		if (ret == TEMPLATE_CUSTOM && templateFile == null) {
			ret = TEMPLATE_UNKNOWN;
		}
		return ret;
	}

	public static void handleGraphicsAcceleration() {
		String accel = GRAPHICS_ACCELERATION.get();
		try {
			if (accel == ACCEL_NONE) {
				System.setProperty("sun.java2d.opengl", "False");
				System.setProperty("sun.java2d.d3d", "False");
			} else if (accel == ACCEL_OPENGL) {
				System.setProperty("sun.java2d.opengl", "True");
				System.setProperty("sun.java2d.d3d", "False");
			} else if (accel == ACCEL_D3D) {
				System.setProperty("sun.java2d.opengl", "False");
				System.setProperty("sun.java2d.d3d", "True");
			}
		} catch (Throwable t) {
		}
	}

	public static void removePropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}

	public static void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(propertyName, listener);
	}

	public static void setLayout() {
		String layout = LOOK_AND_FEEL.get();
		try {
			UIManager.setLookAndFeel(layout);
		} catch (Exception ex) {
			System.err.println("Layout error " + layout);
		}
	}

	public static void setLibrariesFolder(File value) {
		getPrefs();
		if (value != null && !value.canRead())
			value = null;
		if (value == null ? LibrariesFolder != null : !value.equals(LibrariesFolder)) {
			try {
				getPrefs().put(LIBRARIES_FOLDER_PATH, value == null ? "" : value.getCanonicalPath());
			} catch (IOException ex) {
			}
		}
	}

	public static void setTemplateFile(File value) {
		getPrefs();
		setTemplateFile(value, null);
	}

	public static void setTemplateFile(File value, Template template) {
		getPrefs();
		if (value != null && !value.canRead())
			value = null;
		if (value == null ? templateFile != null : !value.equals(templateFile)) {
			try {
				customTemplateFile = template == null ? null : value;
				customTemplate = template;
				getPrefs().put(TEMPLATE_FILE, value == null ? "" : value.getCanonicalPath());
			} catch (IOException ex) {
			}
		}
	}

	public static void setTemplateType(int value) {
		getPrefs();
		if (value != TEMPLATE_PLAIN && value != TEMPLATE_EMPTY && value != TEMPLATE_CUSTOM) {
			value = TEMPLATE_UNKNOWN;
		}
		if (value != TEMPLATE_UNKNOWN && templateType != value) {
			getPrefs().putInt(TEMPLATE_TYPE, value);
		}
	}

	public static void updateRecentFile(File file) {
		recentProjects.updateRecent(file);
	}
}
