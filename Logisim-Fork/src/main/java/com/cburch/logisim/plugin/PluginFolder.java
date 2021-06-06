package com.cburch.logisim.plugin;

import java.io.File;


public class PluginFolder {
	static String pluginPath=null;
	/**
	 * Check if Logisim folder is set and exist.
	 * If not, create it in the user's home.
	 */
	public static void createFolder() {
		
		pluginPath=PluginPreferences.getStringPreference("LOGISIM_PATH");
		File preferencePath;
	            //Plugin Folder not set
            if (pluginPath==null){
                pluginPath=createStandardPath();
                
            }
            else{ //Plugin Folder set 
            	preferencePath=new File (PluginPreferences.getStringPreference("LOGISIM_PATH"));
            	// Plugin Folder exist
                if (preferencePath.exists()){
                    pluginPath=preferencePath.toString();
                }
                else{
                    pluginPath=createStandardPath();
                } 
            }
            PluginPreferences.setStringPreference("LOGISIM_PATH", pluginPath);
	    }
	/**
	 * Create the standard path of Logisim Folders.
	 * Return the path to it.     
	 * @return String
	 */
	public static String createStandardPath(){
        File logisimFolder = new File(System.getProperty("user.home")+File.separator+"Logisim"+File.separator+"Plugin");
        if (!logisimFolder.exists()){
            logisimFolder.mkdirs();
        }
        return logisimFolder.toString();
	}
}
