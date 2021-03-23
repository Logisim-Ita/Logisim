/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.canvas;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import com.cburch.draw.model.CanvasModel;
import com.cburch.draw.model.CanvasObject;
import com.cburch.draw.undo.Action;
import com.cburch.logisim.prefs.AppPreferences;

public class Canvas extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4074830572060662303L;
	public static final String TOOL_PROPERTY = "tool";
	public static final String MODEL_PROPERTY = "model";

	public static int Snap(int p) {
		if (p < 0)
			return -((-p + 5) / 10 * 10);
		else
			return (p + 5) / 10 * 10;
	}

	private CanvasModel model;
	private ActionDispatcher dispatcher;
	private CanvasListener listener;

	private Selection selection;

	public Canvas() {
		model = null;
		listener = new CanvasListener(this);
		selection = new Selection();
		addMouseListener(listener);
		addMouseMotionListener(listener);
		addKeyListener(listener);
		setPreferredSize(new Dimension(400, 400));
	}

	public void doAction(Action action) {
		dispatcher.doAction(action);
	}

	public CanvasModel getModel() {
		return model;
	}

	public Selection getSelection() {
		return selection;
	}

	public CanvasTool getTool() {
		return listener.getTool();
	}

	public double getZoomFactor() {
		return 1.0; // subclass will have to override this
	}

	protected void paintBackground(Graphics g) {
		if (AppPreferences.ANTI_ALIASING.getBoolean()) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		g.clearRect(0, 0, getWidth(), getHeight());
	}

	@Override
	public void paintComponent(Graphics g) {
		if (AppPreferences.ANTI_ALIASING.getBoolean()) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		paintBackground(g);
		paintForeground(g);
	}

	protected void paintForeground(Graphics g) {
		CanvasModel model = this.model;
		CanvasTool tool = listener.getTool();
		if (model != null) {
			Graphics dup = g.create();
			model.paint(g, selection);
			dup.dispose();
		}
		if (tool != null) {
			Graphics dup = g.create();
			tool.draw(this, dup);
			dup.dispose();
		}
	}

	public void repaintCanvasCoords(int x, int y, int width, int height) {
		repaint(x, y, width, height);
	}

	public void setModel(CanvasModel value, ActionDispatcher dispatcher) {
		CanvasModel oldValue = model;
		if (oldValue != value) {
			if (oldValue != null)
				oldValue.removeCanvasModelListener(listener);
			model = value;
			this.dispatcher = dispatcher;
			if (value != null)
				value.addCanvasModelListener(listener);
			selection.clearSelected();
			repaint();
			firePropertyChange(MODEL_PROPERTY, oldValue, value);
		}
	}

	protected void setSelection(Selection value) {
		selection = value;
		repaint();
	}

	public void setTool(CanvasTool value) {
		CanvasTool oldValue = listener.getTool();
		if (value != oldValue) {
			listener.setTool(value);
			firePropertyChange(TOOL_PROPERTY, oldValue, value);
		}
	}

	protected JPopupMenu showPopupMenu(MouseEvent e, CanvasObject clicked) {
		return null; // subclass will override if it supports popup menus
	}

	public int snapX(int x) {
		return Snap(x); // subclass will have to override this
	}

	public int snapY(int y) {
		return Snap(y); // subclass will have to override this
	}

	public void toolGestureComplete(CanvasTool tool, CanvasObject created) {
		; // nothing to do - subclass may override
	}
}
