/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Set;

import com.cburch.logisim.LogisimVersion;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeDefaultProvider;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.gui.main.Canvas;

//
// DRAWING TOOLS
//
public abstract class Tool implements AttributeDefaultProvider {
	private static Cursor dflt_cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);

	public Tool cloneTool() {
		return this;
	}

	public void deselect(Canvas canvas) {
	}

	public void draw(Canvas canvas, ComponentDrawContext context) {
		draw(context);
	}

	// This was the draw method until 2.0.4 - As of 2.0.5, you should
	// use the other draw method.
	public void draw(ComponentDrawContext context) {
	}

	public AttributeSet getAttributeSet() {
		return null;
	}

	public AttributeSet getAttributeSet(Canvas canvas) {
		return getAttributeSet();
	}

	public Cursor getCursor() {
		return dflt_cursor;
	}

	@Override
	public Object getDefaultAttributeValue(Attribute<?> attr, LogisimVersion ver) {
		return null;
	}

	public abstract String getDescription();

	public abstract String getDisplayName();

	public Set<Component> getHiddenComponents(Canvas canvas) {
		return null;
	}

	public abstract String getName();

	@Override
	public boolean isAllDefaultValues(AttributeSet attrs, LogisimVersion ver) {
		return false;
	}

	public void keyPressed(Canvas canvas, KeyEvent e) {
	}

	public void keyReleased(Canvas canvas, KeyEvent e) {
	}

	public void keyTyped(Canvas canvas, KeyEvent e) {
	}

	public void mouseDragged(Canvas canvas, Graphics g, MouseEvent e) {
	}

	public void mouseEntered(Canvas canvas, Graphics g, MouseEvent e) {
	}

	public void mouseExited(Canvas canvas, Graphics g, MouseEvent e) {
	}

	public void mouseMoved(Canvas canvas, Graphics g, MouseEvent e) {
	}

	public void mousePressed(Canvas canvas, Graphics g, MouseEvent e) {
	}

	public void mouseReleased(Canvas canvas, Graphics g, MouseEvent e) {
	}

	public void paintIcon(ComponentDrawContext c, int x, int y) {
	}

	public void select(Canvas canvas) {
	}

	public void setAttributeSet(AttributeSet attrs) {
	}

	public boolean sharesSource(Tool other) {
		return this == other;
	}

	@Override
	public String toString() {
		return getName();
	}

}
