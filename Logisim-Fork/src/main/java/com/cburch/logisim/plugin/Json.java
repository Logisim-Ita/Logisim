package com.cburch.logisim.plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import com.google.gson.Gson;
public class Json{
    static final String plugInJsonLink="http://classiperlo.altervista.org/Beta2018/plugin.json";
	
    static PlugIn[] pluginArray=getPluginArray();
	/**
	 * Search pluginName in the remote array of plugin. Return an array of 2 string.
	 * Index-0 Version
	 * Index-1 Link
	 * @param pluginName
	 * @return String[2]
	 */
    public static String[] searchJson(String pluginName) {
    	String[] result = new String[2];
    	try {
			String localName=pluginName.split("-")[0];
			String localVersion=pluginName.split("-")[1];;
			localVersion=localVersion.substring(0, localVersion.lastIndexOf('.'));
			String localExtention=pluginName.substring(pluginName.lastIndexOf(".")+1);
			result[0]=null;
			result[1]=null;
			for (PlugIn plugIn : pluginArray) {
				if (plugIn.getName().equals(localName)&&plugIn.getType().equals(localExtention)) {
					result[0]=plugIn.getVersion();
					result[1]=plugIn.getLink();
					break;
				}
			}
		} catch (Exception e) {
			}
		return result;
    }
    /**
     * Search if pluginName nead to be updated.
     * If yes update the plugin and return true else return false.
     * @param pluginName
     * @return boolean
     */
    public static boolean searchUpdatedVersion(String pluginName) {
    	
    	boolean isUpdated=false;
		try {
			String localVersion=pluginName.split("-")[1];;
			localVersion=localVersion.substring(0, localVersion.lastIndexOf('.'));
			String[] result = searchJson(pluginName);
			String pluginRemoteVersion=result[0];
			String pluginRemoteLink=result[1];
			
			if (!pluginRemoteVersion.equals(localVersion)) {
				PluginUtils.downloadPlugin(pluginRemoteLink);
				PluginUtils.deleteFile(pluginName);
				isUpdated=true;
				
			}
		} catch (Exception e) {
			}
		return isUpdated;
	}
    /**
     * Return an array of PlugIn from the remote JSON file
     * @return PlygIn[]
     */
	public static PlugIn[] getPluginArray() {
		String json="";
		PlugIn[] pluginArray;
		try {
			URLConnection connection = new URL(plugInJsonLink).openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			connection.connect();
			BufferedReader br  = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
			String line;
			while (null != (line = br.readLine())) {
				json+=line;
			}
			br.close();
			Gson gson = new Gson();
			pluginArray = gson.fromJson(json, PlugIn[].class);
		}
		catch (Exception e ) {
			pluginArray=null;
		}
		return pluginArray;
	}        
	

}
