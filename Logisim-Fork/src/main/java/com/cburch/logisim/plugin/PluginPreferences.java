package com.cburch.logisim.plugin;

import java.util.prefs.Preferences;

public class PluginPreferences {
	static Preferences pref = Preferences.userNodeForPackage(PluginPreferences.class);
	
	//Preferences List
	public static final String LOGISIM_PATH = "LOGISIM_PATH";
	public static final String PLUGIN_HASH = "PLUGIN_HASH";
	public static final String PERSONAL_FOLDER = "PERSONAL_FOLDER";
	public static final String AUTO_UPDATE = "AUTO_UPDATE";
	public static final String AUTO_LOAD = "AUTO_LOAD";
	
    public static void setStringPreference(String preferencesName,String newValue) {
        pref.put(preferencesName, newValue);   
    }
    public static void setBooleanPreference(String preferencesName,Boolean newValue) {
        pref.putBoolean(preferencesName, newValue);   
    }
    public static String getStringPreference (String preferencesName) {
        return pref.get(preferencesName, null);
    }
    public static Boolean getBoleanPreference (String preferencesName) {
        return pref.getBoolean(preferencesName, false);
    }

}
