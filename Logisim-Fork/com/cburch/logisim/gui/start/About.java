/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.start;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.cburch.logisim.Main;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.prefs.AppPreferences;

public class About {
	private static class MyPanel extends JPanel implements AncestorListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1104782885051596715L;
		private final Color headerColor = Value.TRUE_COLOR;
		private final Font headerFont = new Font("Sans Serif", Font.BOLD, 55);
		private final Font versionFont = new Font("Sans Serif", Font.BOLD, 25);
		private final Font copyrightFont = new Font("Sans Serif", Font.ITALIC, 18);
		private AboutCredits credits;
		private PanelThread thread = null;

		public MyPanel() {
			setLayout(null);

			setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
			addAncestorListener(this);
			credits = new AboutCredits();
			credits.setBounds(0, 90, IMAGE_WIDTH, IMAGE_HEIGHT - 90);
			add(credits);
		}

		@Override
		public void ancestorAdded(AncestorEvent arg0) {
			if (thread == null) {
				thread = new PanelThread(this);
				thread.start();
			}
		}

		@Override
		public void ancestorMoved(AncestorEvent arg0) {
		}

		@Override
		public void ancestorRemoved(AncestorEvent arg0) {
			if (thread != null) {
				thread.running = false;
			}
		}

		private void drawText(Graphics g, int x, int y) {
			FontMetrics fm;
			String str;

			g.setColor(headerColor);
			g.setFont(headerFont);
			g.drawString("Logisim", x, y + 45);
			fm = g.getFontMetrics();
			g.setFont(versionFont);
			g.drawString(Main.VERSION_NAME, fm.stringWidth("Logisim") + x + 10, y + 45);
			g.setFont(copyrightFont);
			fm = g.getFontMetrics();
			str = "\u00a9 " + Main.COPYRIGHT_YEAR;
			g.drawString(str, IMAGE_WIDTH - fm.stringWidth(str) - x, y + 6);
		}

		@Override
		public void paintComponent(Graphics g) {
			if (AppPreferences.ANTI_ALIASING.getBoolean()) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}
			super.paintComponent(g);
			drawText(g, 10, 20);
		}
	}

	private static class PanelThread extends Thread {
		private MyPanel panel;
		private boolean running = true;

		PanelThread(MyPanel panel) {
			this.panel = panel;
		}

		@Override
		public void run() {
			long start = System.currentTimeMillis();
			while (running) {
				long elapse = System.currentTimeMillis() - start;
				panel.credits.setScroll((int) elapse);
				panel.repaint();
				try {
					Thread.sleep(1000 / Integer.parseInt(AppPreferences.REFRESH_RATE.get()));
				} catch (InterruptedException ex) {
				}
			}
		}
	}

	static final int IMAGE_WIDTH = 425;

	static final int IMAGE_HEIGHT = 250;

	public static MyPanel getImagePanel() {
		return new MyPanel();
	}

	public static void showAboutDialog(JFrame owner) {
		MyPanel imgPanel = getImagePanel();
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(imgPanel);
		JOptionPane.showMessageDialog(owner, panel, "Logisim " + Main.VERSION_NAME, JOptionPane.PLAIN_MESSAGE);
	}

	private About() {
	}
}