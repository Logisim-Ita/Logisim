/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.io;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
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

public class RGBLed extends InstanceFactory {

	Direction facing;
	Value[] port = new Value[3];

	public RGBLed() {
		super("RGBLed", Strings.getter("RGBledComponent"));
		setAttributes(
				new Attribute[] { StdAttr.FACING, Io.ATTR_OFF_COLOR, Io.ATTR_ACTIVE, StdAttr.LABEL, Io.ATTR_LABEL_LOC,
						StdAttr.LABEL_FONT, Io.ATTR_LABEL_COLOR },
				new Object[] { Direction.WEST, Color.DARK_GRAY, Boolean.TRUE, "", Io.LABEL_CENTER,
						StdAttr.DEFAULT_LABEL_FONT, Color.BLACK });
		setFacingAttribute(StdAttr.FACING);
		setIconName("rgbled.gif");
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		facing = attrs.getValue(StdAttr.FACING);
		return Bounds.create(10, -10, 20, 20).rotate(Direction.WEST, facing, 0, 0);
	}

	@Override
	protected void configureNewInstance(Instance instance) {
		instance.addAttributeListener();
		computeTextField(instance);
		updateports(instance);
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == StdAttr.FACING) {
			instance.recomputeBounds();
			computeTextField(instance);
		} else if (attr == Io.ATTR_LABEL_LOC) {
			computeTextField(instance);
		}
		updateports(instance);
	}

	private void updateports(Instance instance) {
		Port[] port = new Port[3];
		if (facing == Direction.NORTH || facing == Direction.SOUTH) {
			port[0] = new Port(-10, 0, Port.INPUT, 1);
			port[1] = new Port(0, 0, Port.INPUT, 1);
			port[2] = new Port(10, 0, Port.INPUT, 1);
		} else {
			port[0] = new Port(0, -10, Port.INPUT, 1);
			port[1] = new Port(0, 0, Port.INPUT, 1);
			port[2] = new Port(0, 10, Port.INPUT, 1);
		}
		port[0].setToolTip(Strings.getter("Red"));
		port[1].setToolTip(Strings.getter("Green"));
		port[2].setToolTip(Strings.getter("Blue"));
		instance.setPorts(port);
	}

	private void computeTextField(Instance instance) {
		Direction facing = instance.getAttributeValue(StdAttr.FACING);
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
			x = bds.getX() - 2;
			halign = GraphicsUtil.H_RIGHT;
		}
		if (labelLoc == facing) {
			if (labelLoc == Direction.NORTH || labelLoc == Direction.SOUTH) {
				x += 2;
				halign = GraphicsUtil.H_LEFT;
			} else {
				y -= 2;
				valign = GraphicsUtil.V_BOTTOM;
			}
		}

		instance.setTextField(StdAttr.LABEL, StdAttr.LABEL_FONT, x, y, halign, valign);
	}

	@Override
	public void propagate(InstanceState state) {
		for (int i = 0; i < 3; i++) {
			port[i] = state.getPort(i);
		}
	}

	@Override
	public void paintGhost(InstancePainter painter) {
		Graphics g = painter.getGraphics();
		Bounds bds = painter.getBounds();
		int x = bds.getX();
		int y = bds.getY();
		GraphicsUtil.switchToWidth(g, 2);
		g.drawOval(bds.getX() + 1, bds.getY() + 1, bds.getWidth() - 2, bds.getHeight() - 2);
		if (facing == Direction.EAST) {
			g.drawLine(x + 30, y + 1, x + 10, y + 1);
			g.drawLine(x + 30, y + 10, x + 20, y + 10);
			g.drawLine(x + 30, y + 19, x + 10, y + 19);
		} else if (facing == Direction.WEST) {
			g.drawLine(x + 10, y + 1, x - 10, y + 1);
			g.drawLine(x, y + 10, x - 10, y + 10);
			g.drawLine(x + 10, y + 19, x - 10, y + 19);
		} else if (facing == Direction.SOUTH) {
			g.drawLine(x + 1, y + 10, x + 1, y + 30);
			g.drawLine(x + 10, y + 20, x + 10, y + 30);
			g.drawLine(x + 19, y + 10, x + 19, y + 30);
		} else if (facing == Direction.NORTH) {
			g.drawLine(x + 1, y + 10, x + 1, y - 10);
			g.drawLine(x + 10, y, x + 10, y - 10);
			g.drawLine(x + 19, y + 10, x + 19, y - 10);
		}
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		int red = port[0] == Value.TRUE ? 255 : 0;
		int green = port[1] == Value.TRUE ? 255 : 0;
		int blue = port[2] == Value.TRUE ? 255 : 0;
		boolean on = (red != 0 || green != 0 || blue != 0) ? true : false;

		Bounds bds = painter.getBounds().expand(-1);
		int x = bds.getX() - 1;
		int y = bds.getY() - 1;
		Graphics g = painter.getGraphics();
		if (painter.getShowState()) {
			Color onColor = new Color(red, green, blue);
			Color offColor = painter.getAttributeValue(Io.ATTR_OFF_COLOR);
			g.setColor(on ? onColor : offColor);
			g.fillOval(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
		}

		if (facing == Direction.EAST) {
			g.setColor(Color.RED);
			g.drawLine(x + 30, y, x + 10, y);
			g.setColor(Color.GREEN);
			g.drawLine(x + 30, y + 10, x + 20, y + 10);
			g.setColor(Color.BLUE);
			g.drawLine(x + 30, y + 20, x + 10, y + 20);
		} else if (facing == Direction.WEST) {
			g.setColor(Color.RED);
			g.drawLine(x + 10, y, x - 10, y);
			g.setColor(Color.GREEN);
			g.drawLine(x, y + 10, x - 10, y + 10);
			g.setColor(Color.BLUE);
			g.drawLine(x + 10, y + 20, x - 10, y + 20);
		} else if (facing == Direction.SOUTH) {
			g.setColor(Color.RED);
			g.drawLine(x, y + 10, x, y + 30);
			g.setColor(Color.GREEN);
			g.drawLine(x + 10, y + 20, x + 10, y + 30);
			g.setColor(Color.BLUE);
			g.drawLine(x + 20, y + 10, x + 20, y + 30);
		} else if (facing == Direction.NORTH) {
			g.setColor(Color.RED);
			g.drawLine(x, y + 10, x, y - 10);
			g.setColor(Color.GREEN);
			g.drawLine(x + 10, y, x + 10, y - 10);
			g.setColor(Color.BLUE);
			g.drawLine(x + 20, y + 10, x + 20, y - 10);
		}
		g.setColor(Color.BLACK);
		GraphicsUtil.switchToWidth(g, 2);
		g.drawOval(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
		GraphicsUtil.switchToWidth(g, 1);
		g.setColor(painter.getAttributeValue(Io.ATTR_LABEL_COLOR));
		painter.drawLabel();
		painter.drawPorts();
	}
}
