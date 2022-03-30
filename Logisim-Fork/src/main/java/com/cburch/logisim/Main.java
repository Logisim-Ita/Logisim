/*
 * Copyright (c) 2011, Carl Burch.
 * 
 * This file is part of the Logisim source code. The latest
 * version is available at http://www.cburch.com/logisim/.
 *
 * Logisim is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Logisim is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Logisim; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301  USA
 */

package com.cburch.logisim;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JOptionPane;

import com.cburch.logisim.gui.start.Startup;

public class Main {
	// current version
	public static final LogisimVersion VERSION = LogisimVersion.get(2, 16, 1, 4, LogisimVersion.getVariantFromFile());
	// the version of the file you're using, equals to current version if new file
	public static LogisimVersion FILE_VERSION;

	public static final String VERSION_NAME = VERSION.toString();

	public static final short COPYRIGHT_YEAR = (short) Calendar.getInstance().get(Calendar.YEAR);

	/* URL for the automatic updater */
	public static final String UPDATE_URL = "https://raw.githubusercontent.com/Logisim-Ita/Logisim/master/version.xml";

	public static final double JAVA_VERSION = getVersion();

	// here will be saved the file in use to reopen when restarting
	public static ArrayList<String> OpenedFiles = new ArrayList<String>();

	// get the runtinme java version
	private final static double getVersion() {
		String version = System.getProperty("java.version");
		byte pos = (byte) version.indexOf('.', version.indexOf('.') + 1);
		return Double.parseDouble((pos != -1) ? version.substring(0, pos) : version);
	}

	public static void main(String[] args) throws Exception {
		Startup startup = Startup.parseArgs(args);
		if (startup != null) {
			// if it's not command line
			/*
			 * if (!startup.isTty()) { // remove online user when runtime stopped
			 * Runtime.getRuntime().addShutdownHook(new Thread() {
			 * 
			 * @Override public void run() { if (AppPreferences.SEND_DATA.getBoolean())
			 * Startup.runRemotePhpCode(
			 * "http://logisim.altervista.org/LogisimData/OnlineUsers/online.php?val=0"); }
			 * }); // add online user if (AppPreferences.SEND_DATA.getBoolean())
			 * Startup.runRemotePhpCode(
			 * "http://logisim.altervista.org/LogisimData/OnlineUsers/online.php?val=1"); }
			 */
			// search for updates, if true update and restart, else run logisim
			if (!startup.autoUpdate(true, null)) {
				try {
					startup.run();
				} catch (Throwable e) {
					Writer result = new StringWriter();
					PrintWriter printWriter = new PrintWriter(result);
					e.printStackTrace(printWriter);
					JOptionPane.showMessageDialog(null, result.toString());
					System.exit(-1);
				}
			} else {
				/*
				 * if (AppPreferences.SEND_DATA.getBoolean()) Startup.runRemotePhpCode(
				 * "http://logisim.altervista.org/LogisimData/Autoupdates/autoupdates.php?val=1"
				 * );
				 */
				Startup.restart(args);
			}
		} else
			System.exit(-1);
	}
}
