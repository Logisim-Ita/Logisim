/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.menu;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import javax.help.HelpSet;
import javax.help.JHelp;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import com.cburch.logisim.Main;
import com.cburch.logisim.gui.generic.LFrame;
import com.cburch.logisim.gui.start.About;
import com.cburch.logisim.gui.start.Startup;
import com.cburch.logisim.util.MacCompatibility;

class MenuHelp extends JMenu implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 155555694453015305L;
	private LogisimMenuBar menubar;
	private JMenuItem tutorial = new JMenuItem();
	private JMenuItem guide = new JMenuItem();
	private JMenuItem library = new JMenuItem();
	private JMenuItem bug = new JMenuItem();
	private JMenuItem forum = new JMenuItem();
	private JMenuItem update = new JMenuItem();
	private JMenuItem about = new JMenuItem();
	private HelpSet helpSet;
	private String helpSetUrl = "";
	private JHelp helpComponent;
	private LFrame helpFrame;

	public MenuHelp(LogisimMenuBar menubar) {
		this.menubar = menubar;

		// f1 key open library reference
		library.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

		tutorial.addActionListener(this);
		guide.addActionListener(this);
		library.addActionListener(this);
		bug.addActionListener(this);
		forum.addActionListener(this);
		update.addActionListener(this);
		about.addActionListener(this);

		add(tutorial);
		add(guide);
		add(library);
		if (!MacCompatibility.isAboutAutomaticallyPresent()) {
			addSeparator();
			add(bug);
			add(forum);
			addSeparator();
			add(update);
			add(about);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == guide) {
			showHelp("guide");
		} else if (src == tutorial) {
			showHelp("tutorial");
		} else if (src == library) {
			showHelp("libs");
		} else if (src == bug) {
			try {
				Desktop.getDesktop().browse(new URI("https://github.com/Logisim-Ita/Logisim/issues"));
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
		} else if (src == forum) {
			try {
				Desktop.getDesktop().browse(new URI("https://sourceforge.net/p/logisimit/discussion/"));
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
		} else if (src == update) {
			Startup startup = new Startup(true);
			if (startup.autoUpdate(false, menubar.getProject().getFrame())) {
				/*
				 * if (AppPreferences.SEND_DATA.getBoolean()) Startup.runRemotePhpCode(
				 * "http://logisim.altervista.org/LogisimData/Autoupdates/autoupdates.php?val=1"
				 * );
				 */
				Startup.restart(Main.OpenedFiles.toArray(new String[0]));
			}

		} else if (src == about) {
			About.showAboutDialog(menubar.getParentWindow());
		}
	}

	private void disableHelp() {
		guide.setEnabled(false);
		tutorial.setEnabled(false);
		library.setEnabled(false);
	}

	private void loadBroker() {
		String helpUrl = Strings.get("helpsetUrl");
		if (helpUrl == null)
			helpUrl = "doc/doc_en.hs";
		if (helpSet == null || helpFrame == null || !helpUrl.equals(helpSetUrl)) {
			ClassLoader loader = MenuHelp.class.getClassLoader();
			try {
				URL hsURL = HelpSet.findHelpSet(loader, helpUrl);
				if (hsURL == null) {
					disableHelp();
					JOptionPane.showMessageDialog(menubar.getParentWindow(), Strings.get("helpNotFoundError"));
					return;
				}
				helpSetUrl = helpUrl;
				helpSet = new HelpSet(null, hsURL);
				helpComponent = new JHelp(helpSet);
				if (helpFrame == null) {
					helpFrame = new LFrame();
					helpFrame.setTitle(Strings.get("helpWindowTitle"));
					helpFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
					helpFrame.getContentPane().add(helpComponent);
					helpFrame.pack();
					helpFrame.setLocationRelativeTo(null);
				} else {
					helpFrame.getContentPane().removeAll();
					helpFrame.getContentPane().add(helpComponent);
					helpComponent.revalidate();
				}
			} catch (Exception e) {
				disableHelp();
				e.printStackTrace();
				JOptionPane.showMessageDialog(menubar.getParentWindow(), Strings.get("helpUnavailableError"));
				return;
			}
		}
	}

	public void localeChanged() {
		this.setText(Strings.get("helpMenu"));
		if (helpFrame != null) {
			helpFrame.setTitle(Strings.get("helpWindowTitle"));
		}
		tutorial.setText(Strings.get("helpTutorialItem"));
		guide.setText(Strings.get("helpGuideItem"));
		library.setText(Strings.get("helpLibraryItem"));
		bug.setText(Strings.get("ReportBug"));
		forum.setText(Strings.get("Forum"));
		about.setText(Strings.get("helpAboutItem"));
		update.setText(Strings.get("CheckUpdates"));
		if (helpFrame != null) {
			helpFrame.setLocale(Locale.getDefault());
			loadBroker();
		}
	}

	private void showHelp(String target) {
		loadBroker();
		try {
			helpComponent.setCurrentID(target);
			helpFrame.toFront();
			helpFrame.setVisible(true);
		} catch (Exception e) {
			disableHelp();
			e.printStackTrace();
			JOptionPane.showMessageDialog(menubar.getParentWindow(), Strings.get("helpDisplayError"));
		}
	}
}
