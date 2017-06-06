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

import javax.swing.JOptionPane;

import com.cburch.logisim.gui.start.Startup;

public class Main {
	public static void main(String[] args) throws Exception {
		Startup startup = Startup.parseArgs(args);
		if (startup == null) {
			System.exit(0);
		} else {
			// If the auto-updater actually performed an update, then quit the
			// program, otherwise continue with the execution
			if (!startup.autoUpdate(false)) {
				try {
					startup.run();
				} catch (Throwable e) {
					Writer result = new StringWriter();
					PrintWriter printWriter = new PrintWriter(result);
					e.printStackTrace(printWriter);
					JOptionPane.showMessageDialog(null, result.toString());
					System.exit(-1);
				}
			} else System.exit(0);
		}
	}

	public static final LogisimVersion VERSION = LogisimVersion.get(2, 7, 1, 11, "jar");

	public static final String VERSION_NAME = VERSION.toString();
	public static final int COPYRIGHT_YEAR = 2017;

	/**
	 * This flag enables auto-updates. It is true by default, so that users
	 * normally check for updates at startup. On the other hand, this might be
	 * annoying for developers, therefore we let them disable it from the
	 * command line with the '-noupdates' option.
	 */
	public static boolean UPDATE = true;

	/* URL for the automatic updater*/
	
	public static final String UPDATE_URL = "https://raw.githubusercontent.com/LogisimIt/Logisim/master/version.xml";
}
