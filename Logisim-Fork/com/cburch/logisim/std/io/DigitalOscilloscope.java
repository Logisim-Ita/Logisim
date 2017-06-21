/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.io;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.GraphicsUtil;

public class DigitalOscilloscope extends InstanceFactory {

	private static final Attribute<Integer> ATTR_INPUTS = Attributes.forIntegerRange("inputs",
			Strings.getter("gateInputsAttr"), 1, 32);
	private static final Attribute<Integer> ATTR_NSTATE = Attributes.forIntegerRange("nState",
			Strings.getter("NStateAttr"), 1, 128);
	static final Attribute<Boolean> SHOW_CLOCK = Attributes.forBoolean("showclock",
			Strings.getter("ShowClockAttribute"));
	static final Attribute<Boolean> REVERSE = Attributes.forBoolean("ReverseDiagram", Strings.getter("ReverseDiagram"));
	static final Attribute<Color> ATTR_COLOR = Attributes.forColor("color", Strings.getter("BorderColor"));

	private final int border = 10;

	public DigitalOscilloscope() {
		super("Digital Oscilloscope", Strings.getter("DigitalOscilloscopeComponent"));
		int inputs = Integer.valueOf(2);
		int length = Integer.valueOf(30);
		setAttributes(
				new Attribute<?>[] { ATTR_INPUTS, ATTR_NSTATE, SHOW_CLOCK, REVERSE, ATTR_COLOR, StdAttr.LABEL,
						Io.ATTR_LABEL_LOC, StdAttr.LABEL_FONT, Io.ATTR_LABEL_COLOR },
				new Object[] { inputs, length, true, false, new Color(0, 125, 255), "", Direction.NORTH,
						StdAttr.DEFAULT_LABEL_FONT, Color.BLACK });
		setIconName("digitaloscilloscope.gif");
	}

	private void computeTextField(Instance instance) {
		Object labelLoc = instance.getAttributeValue(Io.ATTR_LABEL_LOC);

		Bounds bds = instance.getBounds();
		int x = bds.getX() + bds.getWidth() / 2;
		int y = bds.getY() + bds.getHeight() / 2;
		int halign = GraphicsUtil.H_CENTER;
		int valign = GraphicsUtil.V_CENTER;
		if (labelLoc == Direction.NORTH) {
			y = bds.getY() - 2;
			valign = GraphicsUtil.V_BOTTOM;
		} else if (labelLoc == Direction.SOUTH) {
			y = bds.getY() + bds.getHeight() + 2;
			valign = GraphicsUtil.V_TOP;
		} else if (labelLoc == Direction.EAST) {
			x = bds.getX() + bds.getWidth() + 2;
			halign = GraphicsUtil.H_LEFT;
		} else if (labelLoc == Direction.WEST) {
			x = bds.getX();
			y = bds.getY() - 2;
			valign = GraphicsUtil.V_BOTTOM;
		}
		instance.setTextField(StdAttr.LABEL, StdAttr.LABEL_FONT, x, y, halign, valign);
	}

	@Override
	protected void configureNewInstance(Instance instance) {
		instance.addAttributeListener();
		computeTextField(instance);
		updateports(instance);
	}

	private DiagramState getDiagramState(InstanceState state) {
		int inputs = state.getAttributeValue(ATTR_INPUTS).intValue() + 1;
		int length = state.getAttributeValue(ATTR_NSTATE).intValue();
		DiagramState ret = (DiagramState) state.getData();
		if (ret == null) {
			ret = new DiagramState(inputs, length);
			state.setData(ret);
		} else {
			ret.updateSize(inputs, length);
		}
		return ret;
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		int x = attrs.getValue(ATTR_NSTATE).intValue() * 10 + 2 * border + 15;
		int y = attrs.getValue(SHOW_CLOCK) ? (attrs.getValue(ATTR_INPUTS).intValue() + 1) * 30 + 3 * border
				: attrs.getValue(ATTR_INPUTS).intValue() * 30 + 3 * border;
		int showclock = attrs.getValue(SHOW_CLOCK) ? 30 : 0;
		return Bounds.create(0, -border - showclock, x, y);
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == ATTR_NSTATE || attr == ATTR_INPUTS || attr == SHOW_CLOCK) {
			instance.recomputeBounds();
			updateports(instance);
			computeTextField(instance);
		} else if (attr == Io.ATTR_LABEL_LOC) {
			computeTextField(instance);
		}
	}

	@Override
	public void paintGhost(InstancePainter painter) {
		Bounds bds = painter.getBounds();
		int x = bds.getX();
		int y = bds.getY();
		int width = bds.getWidth();
		int height = bds.getHeight();
		Graphics g = painter.getGraphics();
		GraphicsUtil.switchToWidth(g, 2);
		g.drawRoundRect(x, y, width, height, border, border);
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		Bounds bds = painter.getBounds();
		// if showclock = true all diagram lines are moved down
		int reverse = painter.getAttributeValue(REVERSE) ? 1 : 0;
		int showclock = painter.getAttributeValue(SHOW_CLOCK) ? 1 : 0;
		int x = bds.getX();
		int y = bds.getY();
		int width = bds.getWidth();
		int height = bds.getHeight();
		int inputs = painter.getAttributeValue(ATTR_INPUTS).intValue() + showclock;
		int length = painter.getAttributeValue(ATTR_NSTATE).intValue();
		int jr;
		Graphics g = painter.getGraphics();
		// draw border
		g.setColor(painter.getAttributeValue(ATTR_COLOR));
		g.fillRoundRect(x, y, width, height, border, border);

		g.setColor(new Color(250, 250, 250));
		g.fillRoundRect(x + border, y + border, width - 2 * border, height - 2 * border, border, border);

		g.setColor(Color.BLACK);
		GraphicsUtil.switchToWidth(g, 2);
		g.drawRoundRect(x, y, width, height, border, border);
		g.drawRoundRect(x + border, y + border, width - 2 * border, height - 2 * border, border, border);

		DiagramState diagramstate = getDiagramState(painter);
		GraphicsUtil.switchToWidth(g, 1);
		for (int i = 0; i < inputs; i++) {
			// arrow
			g.fillPolygon(
					new int[] { x + border + 10 * -length * (reverse - 1) + 4 + reverse * 7,
							x + border + 10 * -length * (reverse - 1) + 13 - reverse * 11,
							x + border + 10 * -length * (reverse - 1) + 4 + reverse * 7 },
					new int[] { y + border + i * 30 + 27, y + border + i * 30 + 30, y + border + i * 30 + 33 }, 3);
			g.drawLine(x + border + 10 * -length * (reverse - 1) + 11 * reverse, y + border + i * 30 + 30,
					x + border + 10 * -length * (reverse - 1) + 4 + 11 * reverse, y + border + i * 30 + 30);
			// draw diagram
			for (int j = 0; j < length; j++) {
				jr = reverse == 1 ? length - j - 1 : j;
				// vertical line
				if (jr != 0 && diagramstate.getState(i + (showclock == 0 ? 1 : 0), jr) != diagramstate
						.getState(i + (showclock == 0 ? 1 : 0), jr - 1))
					g.drawLine(x + border + 10 * j + reverse * 25, y + 2 * border + 30 * i,
							x + border + 10 * j + reverse * 25, y + border + 30 * (i + 1));
				// 1 line
				if (diagramstate.getState(i + (showclock == 0 ? 1 : 0), jr)) {
					g.drawLine(x + border + 10 * j + 15 * reverse, y + 2 * border + 30 * i,
							x + border + 10 * (j + 1) + 15 * reverse, y + 2 * border + 30 * i);
					if (jr == length - 1) {
						g.drawLine(x + border + 10 * j + (reverse == 1 ? 15 : 10), y + 2 * border + 30 * i,
								x + border + 10 * j + (reverse == 1 ? 15 : 10), y + border + 30 * (i + 1));
					}
				}
				// 0 line
				else
					g.drawLine(x + border + 10 * j + 15 * reverse, y + border + 30 * (i + 1),
							x + border + 10 * (j + 1) + 15 * reverse, y + border + 30 * (i + 1));
			}
		}

		// draw ports
		for (int i = 1; i < painter.getAttributeValue(ATTR_INPUTS) + 3; i++) {
			painter.drawPort(i);
		}
		painter.drawClock(0, Direction.EAST);
		// draw label
		g.setColor(painter.getAttributeValue(Io.ATTR_LABEL_COLOR));
		painter.drawLabel();
	}

	@Override
	public void propagate(InstanceState state) {
		int inputs = state.getAttributeValue(ATTR_INPUTS).intValue() + 1;
		int length = state.getAttributeValue(ATTR_NSTATE).intValue();
		Value clock = state.getPort(0);
		DiagramState diagramstate = getDiagramState(state);
		// get old value and set new value
		Value lastclock = diagramstate.setLastClock(clock);
		// not disabled, not clear an clock connected
		if (lastclock != Value.UNKNOWN && clock != Value.UNKNOWN && state.getPort(inputs + 1) != Value.TRUE
				&& state.getPort(inputs) != Value.FALSE) {
			// for each front
			if (lastclock != clock) {
				// inputs values
				for (int i = 0; i < inputs; i++) {
					// move back all old values
					for (int j = 0; j < length - 1; j++) {
						diagramstate.setState(i, j, diagramstate.getState(i, j + 1));
					}
					// set new value at the end
					diagramstate.setState(i, length - 1, (state.getPort(i) == Value.TRUE) ? true : false);
				}
			} else {// input's values can change also after clock front because
					// of output's delays (Flip Flop, gates etc..)
				for (int i = 1; i < inputs; i++)
					diagramstate.setState(i, length - 1, (state.getPort(i) == Value.TRUE) ? true : false);
			}
		}
		// clear
		else if (state.getPort(inputs + 1) == Value.TRUE)
			diagramstate.clear();
	}

	private void updateports(Instance instance) {
		int inputs = instance.getAttributeValue(ATTR_INPUTS).intValue();
		Port[] port = new Port[inputs + 3];
		for (int i = 0; i <= inputs; i++) {
			port[i] = new Port(0, 30 * i, Port.INPUT, 1);
		}
		// clear
		port[inputs + 1] = new Port(20, 30 * inputs + 2 * border, Port.INPUT, 1);
		port[inputs + 1].setToolTip(Strings.getter("priorityEncoderEnableInTip"));
		// enable
		port[inputs + 2] = new Port(30, 30 * inputs + 2 * border, Port.INPUT, 1);
		port[inputs + 2].setToolTip(Strings.getter("ClearDiagram"));
		// clock
		port[0].setToolTip(Strings.getter("DigitalOscilloscopeClock"));
		instance.setPorts(port);
	}
}
