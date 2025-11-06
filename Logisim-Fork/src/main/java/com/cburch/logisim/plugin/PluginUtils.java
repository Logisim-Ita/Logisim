package com.cburch.logisim.plugin;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.cburch.logisim.Main;
import com.cburch.logisim.gui.start.Startup;
import com.cburch.logisim.proj.Project;

public class PluginUtils {
	private static PlugIn[] pluginArray=Json.pluginArray;
	/**
	 * Return a Vector<Vector<String>> containing all the data of the installed plugin 
	 * @return Vector<Vector<String>>
	 */
	public static Vector<Vector<String>> getInstallTable() {
		Vector<Vector<String>> rec = new Vector<Vector<String>>();
		try {
			Vector<String> single;
	        File directoryPath = new File(PluginPreferences.getStringPreference("LOGISIM_PATH"));
	      	//List of all files and directories
	        File filesList[] = directoryPath.listFiles();
	            
	        if(filesList!=null) {
	        	for (int i = 0; i < filesList.length; i++) {
	        		String pluginName=filesList[i].getName();
	                String localName=pluginName.split("-")[0];
	                String localVersion=pluginName.split("-")[1];
	                String localType=localVersion.substring(localVersion.lastIndexOf('.'), localVersion.length()).replace(".", "");
	                localVersion= localVersion.substring(0, localVersion.lastIndexOf('.'));
	                single = new Vector<String>();
	                single.add(localName);
	                single.add(localVersion);
	                single.add(localType);
	                rec.add(single);
	            }
	        }
	        else {
	        	rec=null;
	        }
		} catch (Exception e) {
			rec=null;
		}
        
        return rec;
    }
	/**
	 * Return a Vector<Vector<String>> containing all the data of the installable plugin
	 * @return Vector<Vector<String>>
	 */
	public static Vector<Vector<String>> getStoreTable() {
		Vector<Vector<String>> rec=null;
		try {
	        Vector<String> single;
	        if(pluginArray!=null) {
	        	rec = new Vector<Vector<String>>();
		        for (int i = 0; i < pluginArray.length; i++) {
		        	single = new Vector<String>();
		        	if(!folderSearch(pluginArray[i].getName(),pluginArray[i].getType())) {
		                single.add(pluginArray[i].getName());
		                single.add(pluginArray[i].getVersion());
		                single.add(pluginArray[i].getType());
		                single.add(pluginArray[i].getSite());
		                rec.add(single);
		        	}
		        }
	        }
		} catch (Exception e) {
			rec=null;
		}
        return rec;
    }
	
	/**
	 * Search if a plugin exist in Logisim plugin folder.
	 * If exist return true else return false
	 * @param name
	 * @param extention
	 * @return boolean
	 */
    public static boolean folderSearch(String name, String extention) {
    	File directoryPath = new File(PluginPreferences.getStringPreference("LOGISIM_PATH"));
        File filesList[] = directoryPath.listFiles();
        if(filesList!=null) {
        	for (int i = 0; i < filesList.length; i++) {
          		String pluginName=filesList[i].getName();
          		String localName=pluginName.split("-")[0];
          		String localExtention=pluginName.substring(pluginName.lastIndexOf(".")+1);
          		if (localName.equals(name)&&extention.equals(localExtention)) {
          			return true;
          		}
          	}
          }
          return false;
      }
    /**
     * Search plugin in the Logisim plugin folder.
     * If exist return filename if not return null
     * @param name
     * @param extention
     * @return String
     */
    public static String stringFolderSearch(String name,String extention) {
    	try {
    		File directoryPath = new File(PluginPreferences.getStringPreference("LOGISIM_PATH"));
        	//List of all files and directories
    		File filesList[] = directoryPath.listFiles();
    		if(filesList!=null) {
    			for (int i = 0; i < filesList.length; i++) {
    				String pluginName=filesList[i].getName();
	    			String localName=pluginName.split("-")[0];
	          		String localExtention=pluginName.substring(pluginName.lastIndexOf(".")+1);
	          		if (localName.equals(name)&&localExtention.equals(extention)) {
	          			return pluginName;
	          		}
    			}
    		}
		} catch (Exception e) {
		}
          return null;
      }
    /**
     * Return an array of JMenuItem which contains all the available plugin
     * @return JMenuItem[]
     */
    public static JMenuItem[] makeArray() {
    	JMenuItem[] array = null;
    	try {
    		File directoryPath = new File(PluginPreferences.getStringPreference("LOGISIM_PATH"));
            // List of all files and directories
            File filesList[] = directoryPath.listFiles();
            if (filesList.length!=0) {
               array = new JMenuItem[filesList.length];
               for (int i = 0; i < array.length; i++) {
                  String localName = filesList[i].getName().split("-")[0];
                  String extention = filesList[i].getName().substring(filesList[i].getName().lastIndexOf('.') + 1);
                  array[i] = new JMenuItem(localName+" ("+extention+")");
               }
            }
		}catch (Exception e) {
			
		}
        return array;
    }
    /**
     * Show a Confirm Dialog that ask to restart Logisim
     */
    public static void requestRestart() {
	    int result = JOptionPane.showConfirmDialog(null,Strings.get("restartRequiredText"), Strings.get("restartRequiredTitle"),
	            JOptionPane.YES_NO_OPTION,
	            JOptionPane.QUESTION_MESSAGE);
	    if(result == JOptionPane.YES_OPTION){
	    	Startup.restart(Main.OpenedFiles.toArray(new String[0]));
         }else {
         }
    }
    /**
     * Delete the pluginName file.
     * @param pluginName
     */
	public static void deleteFile(String pluginName) {
		File f= new File(PluginPreferences.getStringPreference("LOGISIM_PATH")+File.separator+pluginName); 
		f.delete();
	}
	/**
	 * Check if all plugin are updated.
	 * If at least one plugin is updated return true else return false
	 * @return boolean
	 */
	public static boolean updateAllPlugin() {
		boolean isUpdated=false;
		try {
			File directoryPath = new File(PluginPreferences.getStringPreference("LOGISIM_PATH"));
	      	//List of all files and directories
	      	File filesList[] = directoryPath.listFiles();
	    	for(File file : filesList) {
	    		if (Json.searchUpdatedVersion(file.getName())) {
	    			isUpdated=true;
				}
			}
		} catch (Exception e) {
			System.err.println("Error while updating plug-in");
		}
		
    	return isUpdated;
	}

	public static void loadAllPlugin(Project proj) {
		try {
			File directoryPath = new File(PluginPreferences.getStringPreference("LOGISIM_PATH"));
	      	//List of all files and directories
	      	File filesList[] = directoryPath.listFiles();
	    	for(File file : filesList) {
	    		PluginLoader.load(file.getName(), proj);
			}
		} catch (Exception e) {
			System.err.println("Error while uploading plug-in");
		}
		
	}
	/**
	 * Open the link in the browser relate to the plugin passed by param.
	 * @param name
	 */
	public static void openLink(String name){
		URI link=null;
		if(pluginArray!=null) {
	        for (int i = 0; i < pluginArray.length; i++) {
	        	if (pluginArray[i].getName().equals(name)) {
	        		try {
	        			if(!pluginArray[i].getSite().equals("")) {
	        				link=new URI(pluginArray[i].getSite());
	        			}
					} catch (Exception e) {
					}
	        		
	        		break;
	        	}
	        }
        }
		if (link!=null && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(link);
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * Download the plugin related wit the link passed by argument
	 * @param link
	 */
	public static void downloadPlugin(String link) {
		String name= link.substring(link.lastIndexOf('/') + 1);
	      try {
	    	  URLConnection connection = new URL(link).openConnection();
	    	  connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
	    	  connection.connect();
	    	  InputStream inputStream = connection.getInputStream();
	    	  FileOutputStream fileOS = new FileOutputStream(PluginPreferences.getStringPreference("LOGISIM_PATH")+File.separator+name);
	    	  byte data[] = new byte[1024];
			  int byteContent;
			  while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
				  fileOS.write(data, 0, byteContent);
			  }		
		} catch (IOException e) {
				System.err.println("An error occurred when you trying to download the Plug-In from the server");
		 }
	}
}
