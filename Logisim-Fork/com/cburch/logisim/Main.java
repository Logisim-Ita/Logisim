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

import javax.swing.JOptionPane;

import com.cburch.logisim.gui.start.Startup;
import com.cburch.logisim.prefs.AppPreferences;

public class Main {
	public static final LogisimVersion VERSION = LogisimVersion.get(2, 14, 3, 0, LogisimVersion.getVariantFromFile());

	public static final String VERSION_NAME = VERSION.toString();

	public static final int COPYRIGHT_YEAR = 2018;

	public static final String UPDATE_URL = "https://raw.githubusercontent.com/LogisimIt/Logisim/master/version.xml";

	// here will be saved the file in use to reopen when restarting
	public static ArrayList<String> OpenedFiles = new ArrayList<String>();
	/* URL for the automatic updater */

	public static void main(String[] args) throws Exception {
		Startup startup = Startup.parseArgs(args);
		if (AppPreferences.SEND_DATA.getBoolean())
			Startup.runRemotePhpCode("http://logisim.altervista.org/LogisimData/OnlineUsers/online.php?val=1");
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (AppPreferences.SEND_DATA.getBoolean())
					Startup.runRemotePhpCode("http://logisim.altervista.org/LogisimData/OnlineUsers/online.php?val=0");
			}
		});
		if (startup != null) {
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
				if (AppPreferences.SEND_DATA.getBoolean())
					Startup.runRemotePhpCode(
							"http://logisim.altervista.org/LogisimData/Autoupdates/autoupdates.php?val=1");
				Startup.restart(args);
			}
		}
	}
}
