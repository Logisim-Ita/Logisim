/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.io;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceDataSingleton;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;

public class SevenSegment extends InstanceFactory {
	static Bounds[] SEGMENTS = null;
	static Color DEFAULT_OFF = new Color(220, 220, 220);

	static void drawBase(InstancePainter painter, boolean drawconnectionLines) {
		ensureSegments();
		InstanceDataSingleton data = (InstanceDataSingleton) painter.getData();
		byte summ = (data == null ? 0 : ((Integer) data.getValue()).byteValue());
		Boolean active = painter.getAttributeValue(Io.ATTR_ACTIVE);
		byte desired = (byte) (active == null || active.booleanValue() ? 1 : 0);

		Bounds bds = painter.getBounds();
		int x = bds.getX() + 5;
		int y = bds.getY();

		Graphics g = painter.getGraphics();
		Color onColor = painter.getAttributeValue(Io.ATTR_ON_COLOR);
		Color offColor = painter.getAttributeValue(Io.ATTR_OFF_COLOR);
		Color bgColor = painter.getAttributeValue(Io.ATTR_BACKGROUND);
		painter.drawRoundBounds(bgColor);
		g.setColor(Color.DARK_GRAY);

		// to not overlaps off line with on line
		if (drawconnectionLines) {
			if (((summ >> 5) & 1) == desired && ((summ >> 6) & 1) != desired) {
				g.setColor(offColor);
				drawConnectionLines(g, bds, (byte) 6);
				g.setColor(onColor);
				drawConnectionLines(g, bds, (byte) 5);
			} else {
				g.setColor(((summ >> 5) & 1) == desired ? onColor : offColor);
				drawConnectionLines(g, bds, (byte) 5);
				g.setColor(((summ >> 6) & 1) == desired ? onColor : offColor);
				drawConnectionLines(g, bds, (byte) 6);
			}
		}
		for (byte i = 0; i <= 7; i++) {
			if (painter.getShowState())
				g.setColor(((summ >> i) & 1) == desired ? onColor : offColor);
			if (drawconnectionLines && i != 5 && i != 6)
				drawConnectionLines(g, bds, i);
			if (i < 7) {
				Bounds seg = SEGMENTS[i];
				g.fillRect(x + seg.getX(), y + seg.getY(), seg.getWidth(), seg.getHeight());
			} else
				g.fillOval(x + 28, y + 48, 5, 5); // draw decimal point
		}
		painter.drawPorts();
	}

	private static void drawConnectionLines(Graphics g, Bounds bds, byte which) {
		int x = bds.getX() + 5;
		int y = bds.getY();
		switch (which) {
		case 0:
			// a pin
			g.drawLine(x + 20, y, x + 20, y + SEGMENTS[which].getY());
			break;
		case 1:
			// b pin
			g.drawLine(x + 30, y, x + 30, y + 6);
			g.drawLine(x + 30, y + 6, x + SEGMENTS[which].getX() + SEGMENTS[which].getWidth() / 2, y + 6);
			g.drawLine(x + SEGMENTS[which].getX() + SEGMENTS[which].getWidth() / 2, y + 6,
					x + SEGMENTS[which].getX() + SEGMENTS[which].getWidth() / 2, y + +SEGMENTS[which].getY());
			break;
		case 2:
			// c pin
			g.drawLine(x + 20, y + bds.getHeight(), x + 20, y + bds.getHeight() - 6);
			g.drawLine(x + 20, y + bds.getHeight() - 6, x + SEGMENTS[which].getX() + SEGMENTS[which].getWidth() / 2,
					y + bds.getHeight() - 6);
			g.drawLine(x + SEGMENTS[which].getX() + SEGMENTS[which].getWidth() / 2, y + bds.getHeight() - 6,
					x + SEGMENTS[which].getX() + SEGMENTS[which].getWidth() / 2,
					y + SEGMENTS[which].getY() + SEGMENTS[which].getHeight());
			break;
		case 3:
			// d pin
			g.drawLine(x + 10, y + bds.getHeight(), x + 10, y + SEGMENTS[which].getY() + SEGMENTS[which].getHeight());
			break;
		case 4:
			// e pin
			g.drawLine(x, y + bds.getHeight(), x, y + SEGMENTS[which].getY() + SEGMENTS[which].getHeight());
			break;
		case 5:
			// f pin
			g.drawLine(x + 10, y, x + 10, y + 6);
			g.drawLine(x + 10, y + 6, x + SEGMENTS[which].getX() + SEGMENTS[which].getWidth() / 2, y + 6);
			g.drawLine(x + SEGMENTS[which].getX() + SEGMENTS[which].getWidth() / 2, y + 6,
					x + SEGMENTS[which].getX() + SEGMENTS[which].getWidth() / 2, y + +SEGMENTS[which].getY());
			break;
		case 6:
			// g pin
			g.drawLine(x, y, x, y + 3);
			g.drawLine(x, y + 3, x + 7, y + 3);
			g.drawLine(x + 7, y + 3, x + 7, y + SEGMENTS[which].getY());
			break;
		case 7:
			// dp
			g.drawLine(x + 30, y + bds.getHeight(), x + 30, y + bds.getHeight() - 10);
			break;
		default:
			break;
		}
	}

	static void ensureSegments() {
		if (SEGMENTS == null) {
			SEGMENTS = new Bounds[] { Bounds.create(3, 8, 19, 4), Bounds.create(23, 10, 4, 19),
					Bounds.create(23, 30, 4, 19), Bounds.create(3, 47, 19, 4), Bounds.create(-2, 30, 4, 19),
					Bounds.create(-2, 10, 4, 19), Bounds.create(3, 28, 19, 4) };
		}
	}

	public SevenSegment() {
		super("7-Segment Display", Strings.getter("sevenSegmentComponent"));
		setAttributes(new Attribute[] { Io.ATTR_ON_COLOR, Io.ATTR_OFF_COLOR, Io.ATTR_BACKGROUND, Io.ATTR_ACTIVE },
				new Object[] { new Color(240, 0, 0), DEFAULT_OFF, Io.DEFAULT_BACKGROUND, Boolean.TRUE });
		setOffsetBounds(Bounds.create(-5, 0, 40, 60));
		setIconName("7seg.gif");
		Port[] port = new Port[] { new Port(20, 0, Port.INPUT, 1), new Port(30, 0, Port.INPUT, 1),
				new Port(20, 60, Port.INPUT, 1), new Port(10, 60, Port.INPUT, 1), new Port(0, 60, Port.INPUT, 1),
				new Port(10, 0, Port.INPUT, 1), new Port(0, 0, Port.INPUT, 1), new Port(30, 60, Port.INPUT, 1) };
		char c = 97;// a
		for (byte i = 0; i < 7; i++) {
			port[i].setToolTip(Strings.getter("" + c));
			c++;
		}
		port[7].setToolTip(Strings.getter("dp"));// dot point
		setPorts(port);
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		drawBase(painter, true);
	}

	@Override
	public void propagate(InstanceState state) {
		int summary = 0;
		for (byte i = 0; i < 8; i++) {
			Value val = state.getPort(i);
			if (val == Value.TRUE)
				summary |= 1 << i;
		}
		Object value = Integer.valueOf(summary);
		InstanceDataSingleton data = (InstanceDataSingleton) state.getData();
		if (data == null) {
			state.setData(new InstanceDataSingleton(value));
		} else {
			data.setValue(value);
		}
	}
}
