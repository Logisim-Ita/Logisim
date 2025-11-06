/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JTextField;

import com.cburch.logisim.prefs.AppPreferences;

public class EditableLabelField extends JTextField {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5262273703386928333L;
	static final int FIELD_BORDER = 2;

	public EditableLabelField() {
		super(10);
		setBackground(new Color(255, 255, 255, 128));
		setOpaque(false);
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK),
				BorderFactory.createEmptyBorder(1, 1, 1, 1)));
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (AppPreferences.ANTI_ALIASING.getBoolean()) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		super.paintComponent(g);
	}
}
