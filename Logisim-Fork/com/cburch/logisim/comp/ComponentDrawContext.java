/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.comp;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.WireSet;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.util.GraphicsUtil;

public class ComponentDrawContext {
	private static final byte PIN_OFFS = 3;
	private static final byte PIN_SIZE = 6;

	private java.awt.Component dest;
	private Circuit circuit;
	private CircuitState circuitState;
	private Graphics base;
	private Graphics g;
	private boolean showState;
	private boolean showColor;
	private boolean printView;
	private WireSet highlightedWires;
	private InstancePainter instancePainter;

	public ComponentDrawContext(java.awt.Component dest, Circuit circuit, CircuitState circuitState, Graphics base,
			Graphics g) {
		this(dest, circuit, circuitState, base, g, false);
	}

	public ComponentDrawContext(java.awt.Component dest, Circuit circuit, CircuitState circuitState, Graphics base,
			Graphics g, boolean printView) {
		this.dest = dest;
		this.circuit = circuit;
		this.circuitState = circuitState;
		this.base = base;
		this.g = g;
		this.showState = true;
		this.showColor = true;
		this.printView = printView;
		this.highlightedWires = WireSet.EMPTY;
		this.instancePainter = new InstancePainter(this, null);
	}

	public void drawBounds(Component comp, Bounds bds, Color color) {
		GraphicsUtil.switchToWidth(g, 2);
		if (color != null && (AppPreferences.FILL_COMPONENT_BACKGROUND.getBoolean() || !color.equals(Color.WHITE))) {
			g.setColor(color);
			g.fillRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
		}
		g.setColor(Color.BLACK);
		g.drawRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
		GraphicsUtil.switchToWidth(g, 1);
	}

	//
	// helper methods
	//
	public void drawBounds(Component comp, Color color) {
		drawBounds(comp, comp.getBounds(), color);
	}

	public void drawClock(Component comp, int i, Direction dir) {
		Color curColor = g.getColor();
		g.setColor(Color.BLACK);
		GraphicsUtil.switchToWidth(g, 2);

		EndData e = comp.getEnd(i);
		Location pt = e.getLocation();
		int x = pt.getX();
		int y = pt.getY();
		final int CLK_SZ = 4;
		final int CLK_SZD = CLK_SZ - 1;
		if (dir == Direction.NORTH) {
			g.drawLine(x - CLK_SZD, y - 1, x, y - CLK_SZ);
			g.drawLine(x + CLK_SZD, y - 1, x, y - CLK_SZ);
		} else if (dir == Direction.SOUTH) {
			g.drawLine(x - CLK_SZD, y + 1, x, y + CLK_SZ);
			g.drawLine(x + CLK_SZD, y + 1, x, y + CLK_SZ);
		} else if (dir == Direction.EAST) {
			g.drawLine(x + 1, y - CLK_SZD, x + CLK_SZ, y);
			g.drawLine(x + 1, y + CLK_SZD, x + CLK_SZ, y);
		} else if (dir == Direction.WEST) {
			g.drawLine(x - 1, y - CLK_SZD, x - CLK_SZ, y);
			g.drawLine(x - 1, y + CLK_SZD, x - CLK_SZ, y);
		}

		g.setColor(curColor);
		GraphicsUtil.switchToWidth(g, 1);
	}

	public void drawDongle(int x, int y) {
		GraphicsUtil.switchToWidth(g, 2);
		g.drawOval(x - 4, y - 4, 9, 9);
	}

	public void drawHandle(int x, int y) {
		g.setColor(Color.white);
		g.fillRect(x - 3, y - 3, 7, 7);
		g.setColor(Color.black);
		g.drawRect(x - 3, y - 3, 7, 7);
	}

	public void drawHandle(Location loc) {
		drawHandle(loc.getX(), loc.getY());
	}

	public void drawHandles(Component comp) {
		Bounds b = comp.getBounds(g);
		int left = b.getX();
		int right = left + b.getWidth();
		int top = b.getY();
		int bot = top + b.getHeight();
		drawHandle(right, top);
		drawHandle(left, bot);
		drawHandle(right, bot);
		drawHandle(left, top);
	}

	public void drawPin(Component comp, int i) {
		EndData e = comp.getEnd(i);
		Location pt = e.getLocation();
		Color curColor = g.getColor();
		Color pinColor = Color.BLACK;
		int x = pt.getX();
		int y = pt.getY();
		byte endType = e.getType();
		if (getShowState()) {
			CircuitState state = getCircuitState();
			pinColor = state.getValue(pt).getColor();
		}
		// pin
		g.setColor(pinColor);
		if (endType == 1)// input
			g.fillRect(x - PIN_OFFS, y - PIN_OFFS, PIN_SIZE, PIN_SIZE);
		else if (endType == 2)// output
			g.fillOval(x - PIN_OFFS, y - PIN_OFFS, PIN_SIZE, PIN_SIZE);
		else// input-output
			g.fillPolygon(new int[] { x, x - PIN_OFFS - 1, x, x + PIN_OFFS + 1 },
					new int[] { y - PIN_OFFS - 1, y, y + PIN_OFFS + 1, y }, 4);
		g.setColor(curColor);
	}

	public void drawPin(Component comp, int i, String label, Direction dir) {
		Color curColor = g.getColor();
		Color pinColor = Color.BLACK;
		if (i < 0 || i >= comp.getEnds().size())
			return;
		EndData e = comp.getEnd(i);
		Location pt = e.getLocation();
		int x = pt.getX();
		int y = pt.getY();
		byte endType = e.getType();
		if (getShowState()) {
			CircuitState state = getCircuitState();
			pinColor = state.getValue(pt).getColor();
		}
		// pin
		g.setColor(pinColor);
		if (endType == 1)// input
			g.fillRect(x - PIN_OFFS, y - PIN_OFFS, PIN_SIZE, PIN_SIZE);
		else if (endType == 2)// output
			g.fillOval(x - PIN_OFFS, y - PIN_OFFS, PIN_SIZE, PIN_SIZE);
		else// input-output
			g.fillPolygon(new int[] { x, x - PIN_OFFS - 1, x, x + PIN_OFFS + 1 },
					new int[] { y - PIN_OFFS - 1, y, y + PIN_OFFS + 1, y }, 4);
		g.setColor(curColor);
		if (dir == Direction.EAST) {
			GraphicsUtil.drawText(g, label, x + 4, y, GraphicsUtil.H_LEFT, GraphicsUtil.V_CENTER_OVERALL);
		} else if (dir == Direction.WEST) {
			GraphicsUtil.drawText(g, label, x - 4, y, GraphicsUtil.H_RIGHT, GraphicsUtil.V_CENTER_OVERALL);
		} else if (dir == Direction.SOUTH) {
			GraphicsUtil.drawText(g, label, x, y - 4, GraphicsUtil.H_CENTER, GraphicsUtil.V_BASELINE);
		} else if (dir == Direction.NORTH) {
			GraphicsUtil.drawText(g, label, x, y + 4, GraphicsUtil.H_CENTER, GraphicsUtil.V_TOP);
		}
	}

	public void drawPins(Component comp) {
		Color curColor = g.getColor();
		Color pinColor = Color.BLACK;
		for (EndData e : comp.getEnds()) {
			byte endType = e.getType();
			Location pt = e.getLocation();
			int x = pt.getX();
			int y = pt.getY();
			if (getShowState()) {
				CircuitState state = getCircuitState();
				pinColor = state.getValue(pt).getColor();
			}
			// pin
			g.setColor(pinColor);
			if (endType == 1)// input
				g.fillRect(x - PIN_OFFS, y - PIN_OFFS, PIN_SIZE, PIN_SIZE);
			else if (endType == 2)// output
				g.fillOval(x - PIN_OFFS, y - PIN_OFFS, PIN_SIZE, PIN_SIZE);
			else// input-output
				g.fillPolygon(new int[] { x, x - PIN_OFFS - 1, x, x + PIN_OFFS + 1 },
						new int[] { y - PIN_OFFS - 1, y, y + PIN_OFFS + 1, y }, 4);
		}
		g.setColor(curColor);
	}

	public void drawRectangle(Component comp) {
		drawRectangle(comp, "");
	}

	public void drawRectangle(Component comp, String label) {
		Bounds bds = comp.getBounds(g);
		drawRoundRectangle(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight(), label, Color.WHITE);
	}

	public void drawRectangle(ComponentFactory source, int x, int y, AttributeSet attrs, String label) {
		Bounds bds = source.getOffsetBounds(attrs);
		drawRoundRectangle(source, x + bds.getX(), y + bds.getY(), bds.getWidth(), bds.getHeight(), label, Color.WHITE);
	}

	public void drawRoundBounds(Component comp, Bounds bds, Color color) {
		GraphicsUtil.switchToWidth(g, 2);
		if (color != null && (AppPreferences.FILL_COMPONENT_BACKGROUND.getBoolean() || !color.equals(Color.WHITE))) {
			g.setColor(color);
			g.fillRoundRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight(), 10, 10);
		}
		g.setColor(Color.BLACK);
		g.drawRoundRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight(), 10, 10);
		GraphicsUtil.switchToWidth(g, 1);
	}

	public void drawRoundBounds(Component comp, Color color) {
		drawRoundBounds(comp, comp.getBounds(), color);
	}

	public void drawRoundRectangle(ComponentFactory source, int x, int y, int width, int height, String label,
			Color color) {
		GraphicsUtil.switchToWidth(g, 2);
		if (color != null && AppPreferences.FILL_COMPONENT_BACKGROUND.getBoolean() || !color.equals(Color.WHITE)) {
			g.setColor(color);
			g.fillRoundRect(x, y, width, height, 10, 10);
		}
		g.setColor(Color.BLACK);
		g.drawRoundRect(x + 1, y + 1, width - 1, height - 1, 10, 10);
		if (label != null && !label.equals("")) {
			FontMetrics fm = base.getFontMetrics(g.getFont());
			int lwid = fm.stringWidth(label);
			if (height > 20) { // centered at top edge
				g.drawString(label, x + (width - lwid) / 2, y + 2 + fm.getAscent());
			} else { // centered overall
				g.drawString(label, x + (width - lwid) / 2, y + (height + fm.getAscent()) / 2 - 1);
			}
		}
	}

	public void drawRoundRectangle(int x, int y, int width, int height, String label, Color color) {
		GraphicsUtil.switchToWidth(g, 2);
		if (color != null && AppPreferences.FILL_COMPONENT_BACKGROUND.getBoolean() || !color.equals(Color.WHITE)) {
			g.setColor(color);
			g.fillRoundRect(x, y, width, height, 10, 10);
		}
		g.setColor(Color.BLACK);
		g.drawRoundRect(x, y, width, height, 10, 10);
		if (label != null && !label.equals("")) {
			FontMetrics fm = base.getFontMetrics(g.getFont());
			int lwid = fm.stringWidth(label);
			if (height > 20) { // centered at top edge
				g.drawString(label, x + (width - lwid) / 2, y + 2 + fm.getAscent());
			} else { // centered overall
				g.drawString(label, x + (width - lwid) / 2, y + (height + fm.getAscent()) / 2 - 1);
			}
		}
	}

	public Circuit getCircuit() {
		return circuit;
	}

	public CircuitState getCircuitState() {
		return circuitState;
	}

	public java.awt.Component getDestination() {
		return dest;
	}

	public Object getGateShape() {
		return AppPreferences.GATE_SHAPE.get();
	}

	public Graphics getGraphics() {
		return g;
	}

	public WireSet getHighlightedWires() {
		return highlightedWires;
	}

	public InstancePainter getInstancePainter() {
		return instancePainter;
	}

	public boolean getShowState() {
		return !printView && showState;
	}

	public boolean isPrintView() {
		return printView;
	}

	public void setGraphics(Graphics g) {
		this.g = g;
	}

	public void setHighlightedWires(WireSet value) {
		this.highlightedWires = value == null ? WireSet.EMPTY : value;
	}

	public void setShowColor(boolean value) {
		showColor = value;
	}

	public void setShowState(boolean value) {
		showState = value;
	}

	public boolean shouldDrawColor() {
		return !printView && showColor;
	}

}
