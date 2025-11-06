package com.cburch.logisim.plugin;

import java.io.File;

import com.cburch.logisim.gui.menu.ProjectLibraryActions;
import com.cburch.logisim.proj.Project;

public class PluginLoader {
	   /**
	    * Load the pluginName in the project by pluginName.
	    * @param pluginName
	    * @param proj
	    */
	   public static void add(String pluginName, Project proj) {
		 try {
			 String nameNoVersion =pluginName.substring(0,pluginName.lastIndexOf("(")-1);
			 String extention = pluginName.substring(pluginName.indexOf("(")+1,pluginName.lastIndexOf(')'));
			 String name=File.separator+PluginUtils.stringFolderSearch(nameNoVersion, extention);
			 File file= new File(PluginPreferences.getStringPreference("LOGISIM_PATH")+name);
					if (extention.equals("circ")) {
						ProjectLibraryActions.LoadLogisimLibrary(proj, file);
					}
					else if (extention.equals("jar")){
						ProjectLibraryActions.LoadJarLibrary(proj, file);
					}
		} catch (Exception e) {
		}
	   }
	   /**
	    * Load pluginName in the project. pluginName is in standard Logisim plugin filename.
	    * @param pluginName
	    * @param proj
	    */
	   public static void load(String pluginName, Project proj) {
			 try {
				 String extention = pluginName.substring(pluginName.lastIndexOf('.') + 1);
				 File file= new File(PluginPreferences.getStringPreference("LOGISIM_PATH")+File.separator+pluginName);
						if (extention.equals("circ")) {
							ProjectLibraryActions.LoadLogisimLibrary(proj, file);
						}
						else if (extention.equals("jar")){
							ProjectLibraryActions.LoadJarLibrary(proj, file);
						}
			} catch (Exception e) {
			}
	   }
	   /**
	    * Check if the missing library is a plugin.
	    * If true return the new path. If false return the pluginPath
	    * @param pluginPath
	    * @return String
	    */
	   public static String check(String pluginPath) {
			try {
				String separator;
				if(pluginPath.indexOf("/")!=-1)
						separator="/";
				else
					separator="\\";
				
				String pluginName=pluginPath.substring(pluginPath.lastIndexOf(separator)).replace(separator, "");
				String localName=pluginName.split("-")[0];
				String localType=pluginName.substring(pluginName.lastIndexOf(".")+1);
				// Search the name and the type of plugin in the Logisim plugins folder
				String newPath=PluginUtils.stringFolderSearch(localName,localType);
				if (newPath!=null){
					pluginPath=PluginPreferences.getStringPreference(PluginPreferences.LOGISIM_PATH)+File.separator+newPath;
				}
				return pluginPath;
			} catch (Exception e) {
				return pluginPath;
			}
		}
}	   
		
	   
		
