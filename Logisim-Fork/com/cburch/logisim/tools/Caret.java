/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.cburch.logisim.data.Bounds;

public interface Caret {
	// listener methods
	public void addCaretListener(CaretListener e);

	public void cancelEditing();

	// finishing
	public void commitText(String text);

	public void draw(Graphics g);

	public Bounds getBounds(Graphics g);

	// query/Graphics methods
	public String getText();

	public void keyPressed(KeyEvent e);

	public void keyReleased(KeyEvent e);

	public void keyTyped(KeyEvent e);

	public void mouseDragged(MouseEvent e);

	// events to handle
	public void mousePressed(MouseEvent e);

	public void mouseReleased(MouseEvent e);

	public void removeCaretListener(CaretListener e);

	public void stopEditing();
}
