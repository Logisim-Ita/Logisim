/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.io;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.GraphicsUtil;

public class RGBLed extends InstanceFactory {

	public RGBLed() {
		super("RGBLed", Strings.getter("rgbledComponent"));
		setAttributes(
				new Attribute[] { StdAttr.FACING, Io.MULTI_BIT, StdAttr.LABEL, Io.ATTR_LABEL_LOC, StdAttr.LABEL_FONT,
						StdAttr.ATTR_LABEL_COLOR },
				new Object[] { Direction.WEST, false, "", Direction.NORTH, StdAttr.DEFAULT_LABEL_FONT, Color.BLACK });
		setFacingAttribute(StdAttr.FACING);
		setIconName("rgbled.gif");
	}

	private void computeTextField(Instance instance) {
		Direction facing = instance.getAttributeValue(StdAttr.FACING);
		Object labelLoc = instance.getAttributeValue(Io.ATTR_LABEL_LOC);

		Bounds bds = instance.getBounds();
		int x = bds.getX() + bds.getWidth() / 2;
		int y = bds.getY() + bds.getHeight() / 2;
		int halign = GraphicsUtil.H_CENTER;
		int valign = GraphicsUtil.V_CENTER_OVERALL;
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
				x += 12;
				halign = GraphicsUtil.H_LEFT;
			} else {
				y -= 12;
				valign = GraphicsUtil.V_BOTTOM;
			}
		}

		instance.setTextField(StdAttr.LABEL, StdAttr.LABEL_FONT, StdAttr.ATTR_LABEL_COLOR, x, y, halign, valign);
	}

	@Override
	protected void configureNewInstance(Instance instance) {
		instance.addAttributeListener();
		computeTextField(instance);
		updateports(instance);
	}

	public void drawInstance(InstancePainter painter, boolean isGhost) {
		Direction facing = painter.getAttributeValue(StdAttr.FACING);
		Bounds bds = painter.getBounds();
		int x = bds.getX();
		int y = bds.getY();
		int cx = 0, cy = 0, cw = 0, ch = 0;
		Graphics g = painter.getGraphics();
		GraphicsUtil.switchToWidth(g, 2);
		if (facing == Direction.EAST) {
			if (!isGhost)
				g.setColor(Color.RED);
			g.drawLine(x + 30, y, x + 10, y);
			if (!isGhost)
				g.setColor(Color.GREEN);
			g.drawLine(x + 30, y + 10, x + 20, y + 10);
			if (!isGhost)
				g.setColor(Color.BLUE);
			g.drawLine(x + 30, y + 20, x + 10, y + 20);
			cx = bds.getX();
			cy = bds.getY();
			cw = bds.getWidth() - 10;
			ch = bds.getHeight();
		} else if (facing == Direction.WEST) {
			if (!isGhost)
				g.setColor(Color.RED);
			g.drawLine(x + 20, y, x, y);
			if (!isGhost)
				g.setColor(Color.GREEN);
			g.drawLine(x + 10, y + 10, x, y + 10);
			if (!isGhost)
				g.setColor(Color.BLUE);
			g.drawLine(x + 20, y + 20, x, y + 20);
			cx = bds.getX() + 10;
			cy = bds.getY();
			cw = bds.getWidth() - 10;
			ch = bds.getHeight();
		} else if (facing == Direction.SOUTH) {
			if (!isGhost)
				g.setColor(Color.RED);
			g.drawLine(x, y + 10, x, y + 30);
			if (!isGhost)
				g.setColor(Color.GREEN);
			g.drawLine(x + 10, y + 20, x + 10, y + 30);
			if (!isGhost)
				g.setColor(Color.BLUE);
			g.drawLine(x + 20, y + 10, x + 20, y + 30);
			cx = bds.getX();
			cy = bds.getY();
			cw = bds.getWidth();
			ch = bds.getHeight() - 10;
		} else if (facing == Direction.NORTH) {
			if (!isGhost)
				g.setColor(Color.RED);
			g.drawLine(x, y + 20, x, y);
			if (!isGhost)
				g.setColor(Color.GREEN);
			g.drawLine(x + 10, y + 10, x + 10, y);
			if (!isGhost)
				g.setColor(Color.BLUE);
			g.drawLine(x + 20, y + 20, x + 20, y);
			cx = bds.getX();
			cy = bds.getY() + 10;
			cw = bds.getWidth();
			ch = bds.getHeight() - 10;
		}
		if (!isGhost) {
			if (painter.getShowState()) {
				// value=0->input false, value=-1->input unknown
				int red = (painter.getPort(0).toIntValue() > 0)
						? (painter.getAttributeValue(Io.MULTI_BIT)) ? painter.getPort(0).toIntValue() : 255
						: 0;
				int green = (painter.getPort(1).toIntValue() > 0)
						? (painter.getAttributeValue(Io.MULTI_BIT)) ? painter.getPort(1).toIntValue() : 255
						: 0;
				int blue = (painter.getPort(2).toIntValue() > 0)
						? (painter.getAttributeValue(Io.MULTI_BIT)) ? painter.getPort(2).toIntValue() : 255
						: 0;
				GraphicsUtil.switchToWidth(g, 1);
				Color onColor = new Color(red, green, blue);
				g.setColor(onColor);
				g.fillOval(cx, cy, cw, ch);
			}
			GraphicsUtil.switchToWidth(g, 2);
			g.setColor(Color.BLACK);
		}
		g.drawOval(cx, cy, cw, ch);
		GraphicsUtil.switchToWidth(g, 1);
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		Direction facing = attrs.getValue(StdAttr.FACING);
		return Bounds.create(0, -10, 30, 20).rotate(Direction.WEST, facing, 0, 0);
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == Io.MULTI_BIT)
			updateports(instance);
		else if (attr == StdAttr.FACING) {
			instance.recomputeBounds();
			computeTextField(instance);
			updateports(instance);
		} else if (attr == Io.ATTR_LABEL_LOC)
			computeTextField(instance);
	}

	@Override
	public void paintGhost(InstancePainter painter) {
		drawInstance(painter, true);
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		drawInstance(painter, false);
		painter.drawLabel();
		painter.drawPorts();
	}

	@Override
	public void propagate(InstanceState state) {
	}

	private void updateports(Instance instance) {
		Direction facing = instance.getAttributeValue(StdAttr.FACING);
		BitWidth bits = (instance.getAttributeValue(Io.MULTI_BIT)) ? BitWidth.create(8) : BitWidth.ONE;
		Port[] port = new Port[3];
		if (facing == Direction.NORTH || facing == Direction.SOUTH) {
			port[0] = new Port(-10, 0, Port.INPUT, bits);
			port[1] = new Port(0, 0, Port.INPUT, bits);
			port[2] = new Port(10, 0, Port.INPUT, bits);
		} else {
			port[0] = new Port(0, -10, Port.INPUT, bits);
			port[1] = new Port(0, 0, Port.INPUT, bits);
			port[2] = new Port(0, 10, Port.INPUT, bits);
		}
		port[0].setToolTip(Strings.getter("Red"));
		port[1].setToolTip(Strings.getter("Green"));
		port[2].setToolTip(Strings.getter("Blue"));
		instance.setPorts(port);
	}
}
