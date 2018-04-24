/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.io;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.GraphicsUtil;

public class Joystick extends InstanceFactory {
	public static class Poker extends InstancePoker {
		@Override
		public void mouseDragged(InstanceState state, MouseEvent e) {
			Bounds bds = state.getInstance().getBounds();
			int cx = bds.getX() + 15;
			int cy = bds.getY() + 15;
			updateState(state, e.getX() - cx, e.getY() - cy);
		}

		@Override
		public void mousePressed(InstanceState state, MouseEvent e) {
			mouseDragged(state, e);
		}

		@Override
		public void mouseReleased(InstanceState state, MouseEvent e) {
			updateState(state, 0, 0);
		}

		@Override
		public void paint(InstancePainter painter) {
			State state = (State) painter.getData();
			if (state == null) {
				state = new State(0, 0);
				painter.setData(state);
			}
			Bounds bds = painter.getBounds();
			int x = bds.getX() + 30;
			int y = bds.getY() + 15;
			Graphics g = painter.getGraphics();
			g.setColor(Color.BLACK);
			g.fillOval(x - 19, y - 4, 8, 8);
			GraphicsUtil.switchToWidth(g, 3);
			int dx = state.xPos;
			int dy = state.yPos;
			int x0 = x - 15 + (dx > 5 ? 1 : dx < -5 ? -1 : 0);
			int y0 = y + (dy > 5 ? 1 : dy < 0 ? -1 : 0);
			int x1 = x - 15 + dx;
			int y1 = y + dy;
			g.drawLine(x0, y0, x1, y1);
			Color ballColor = painter.getAttributeValue(Io.ATTR_COLOR);
			Joystick.drawBall(g, x1, y1, ballColor, true);
		}

		private void updateState(InstanceState state, int dx, int dy) {
			State s = (State) state.getData();
			if (dx < -14)
				dx = -14;
			if (dy < -14)
				dy = -14;
			if (dx > 14)
				dx = 14;
			if (dy > 14)
				dy = 14;
			if (s == null) {
				s = new State(dx, dy);
				state.setData(s);
			} else {
				s.xPos = dx;
				s.yPos = dy;
			}
			state.getInstance().fireInvalidated();
		}
	}

	private static class State implements InstanceData, Cloneable {
		private int xPos;
		private int yPos;

		public State(int x, int y) {
			xPos = x;
			yPos = y;
		}

		@Override
		public Object clone() {
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
	}

	static final Attribute<BitWidth> ATTR_WIDTH = Attributes.forBitWidth("bits", Strings.getter("ioBitWidthAttr"), 2,
			5);

	private static void drawBall(Graphics g, int x, int y, Color c, boolean inColor) {
		if (inColor) {
			g.setColor(c == null ? Color.RED : c);
		} else {
			int hue = c == null ? 128 : (c.getRed() + c.getGreen() + c.getBlue()) / 3;
			g.setColor(new Color(hue, hue, hue));
		}
		GraphicsUtil.switchToWidth(g, 1);
		g.fillOval(x - 4, y - 4, 8, 8);
		g.setColor(Color.BLACK);
		g.drawOval(x - 4, y - 4, 8, 8);
	}

	public Joystick() {
		super("Joystick", Strings.getter("joystickComponent"));
		setAttributes(new Attribute[] { StdAttr.FACING, ATTR_WIDTH, Io.ATTR_COLOR, StdAttr.ATTR_LABEL_COLOR },
				new Object[] { Direction.EAST, BitWidth.create(4), Color.RED, Color.BLACK });

		setIconName("joystick.gif");
		setFacingAttribute(StdAttr.FACING);
		setInstancePoker(Poker.class);
	}

	@Override
	protected void configureNewInstance(Instance instance) {
		instance.addAttributeListener();
		updateports(instance);
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		Direction dir = attrs.getValue(StdAttr.FACING);
		if (dir == Direction.EAST || dir == Direction.WEST)
			return Bounds.create(-30, -10, 30, 30).rotate(Direction.EAST, dir, 0, 5);
		else
			return Bounds.create(-20, -30, 30, 30).rotate(Direction.NORTH, dir, -5, 0);

	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == StdAttr.FACING || attr == ATTR_WIDTH) {
			instance.recomputeBounds();
			updateports(instance);
		}
	}

	@Override
	public void paintGhost(InstancePainter painter) {
		Bounds bds = painter.getBounds();
		int x = bds.getX();
		int y = bds.getY();
		Graphics g = painter.getGraphics();
		GraphicsUtil.switchToWidth(g, 2);
		g.drawRoundRect(x, y, 30, 30, 8, 8);
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		Bounds bds = painter.getBounds();
		int x = bds.getX() + 30;
		int y = bds.getY() + 15;

		Graphics g = painter.getGraphics();
		painter.drawRoundBounds(Color.WHITE);
		g.drawRoundRect(x - 27, y - 12, 24, 24, 5, 5);
		drawBall(g, x - 15, y, painter.getAttributeValue(Io.ATTR_COLOR), painter.shouldDrawColor());
		painter.drawPorts();
	}

	@Override
	public void propagate(InstanceState state) {
		BitWidth bits = state.getAttributeValue(ATTR_WIDTH);
		int dx;
		int dy;
		State s = (State) state.getData();
		if (s == null) {
			dx = 0;
			dy = 0;
		} else {
			dx = s.xPos;
			dy = s.yPos;
		}

		int steps = (1 << bits.getWidth()) - 1;
		dx = (dx + 14) * steps / 29 + 1;
		dy = (dy + 14) * steps / 29 + 1;
		if (bits.getWidth() > 4) {
			if (dx >= steps / 2)
				dx++;
			if (dy >= steps / 2)
				dy++;
		}
		state.setPort(0, Value.createKnown(bits, dx), 1);
		state.setPort(1, Value.createKnown(bits, dy), 1);
	}

	private void updateports(Instance instance) {
		Direction dir = instance.getAttributeValue(StdAttr.FACING);
		BitWidth bw = instance.getAttributeValue(ATTR_WIDTH);
		Port[] ports = new Port[2];
		if (dir == Direction.EAST || dir == Direction.WEST) {
			ports[0] = new Port(0, 0, Port.OUTPUT, bw);
			ports[1] = new Port(0, 10, Port.OUTPUT, bw);
		} else {
			ports[0] = new Port(0, 0, Port.OUTPUT, bw);
			ports[1] = new Port(-10, 0, Port.OUTPUT, bw);
		}
		ports[0].setToolTip(Strings.getter("X"));
		ports[1].setToolTip(Strings.getter("Y"));

		instance.setPorts(ports);

	}

}
