/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.io;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceDataSingleton;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstanceLogger;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.GraphicsUtil;

//all based on normal button
public class Switch extends InstanceFactory {
	public static class Logger extends InstanceLogger {
		@Override
		public String getLogName(InstanceState state, Object option) {
			return state.getAttributeValue(StdAttr.LABEL);
		}

		@Override
		public Value getLogValue(InstanceState state, Object option) {
			InstanceDataSingleton data = (InstanceDataSingleton) state.getData();
			return data == null ? Value.FALSE : (Value) data.getValue();
		}
	}

	public static class Poker extends InstancePoker {
		@Override
		public void mouseReleased(InstanceState state, MouseEvent e) {
			InstanceDataSingleton data = (InstanceDataSingleton) state.getData();
			Value val = data == null ? Value.FALSE : (Value) data.getValue();
			setValue(state, val.not());// opposite value
		}

		private void setValue(InstanceState state, Value val) {
			InstanceDataSingleton data = (InstanceDataSingleton) state.getData();
			if (data == null) {
				state.setData(new InstanceDataSingleton(val));
			} else {
				data.setValue(val);
			}
			state.getInstance().fireInvalidated();
		}
	}

	private static final int DEPTH = 3;

	public Switch() {
		super("Switch", Strings.getter("switchComponent"));
		setAttributes(
				new Attribute[] { StdAttr.FACING, Io.ATTR_COLOR, StdAttr.LABEL, Io.ATTR_LABEL_LOC, StdAttr.LABEL_FONT,
						Io.ATTR_LABEL_COLOR },
				new Object[] { Direction.EAST, Color.WHITE, "",  Direction.WEST, StdAttr.DEFAULT_LABEL_FONT,
						Color.BLACK });
		setFacingAttribute(StdAttr.FACING);
		setIconName("switch.gif");
		setPorts(new Port[] { new Port(0, 0, Port.OUTPUT, 1) });
		setInstancePoker(Poker.class);
		setInstanceLogger(Logger.class);
	}

	private void computeTextField(Instance instance) {
		Direction facing = instance.getAttributeValue(StdAttr.FACING);
		Object labelLoc = instance.getAttributeValue(Io.ATTR_LABEL_LOC);

		Bounds bds = instance.getBounds();
		int x = bds.getX() + bds.getWidth() / 2;
		int y = bds.getY() + bds.getHeight() / 2;
		int halign = GraphicsUtil.H_CENTER;
		int valign = GraphicsUtil.V_CENTER;
		if (labelLoc == Io.LABEL_CENTER) {
			x = bds.getX() + (bds.getWidth() - DEPTH) / 2;
			y = bds.getY() + (bds.getHeight() - DEPTH) / 2;
		} else if (labelLoc == Direction.NORTH) {
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
	protected void configureNewInstance(Instance instance) {
		instance.addAttributeListener();
		computeTextField(instance);
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		Direction facing = attrs.getValue(StdAttr.FACING);
		// changed to a rectangle
		return Bounds.create(-20, -15, 20, 30).rotate(Direction.EAST, facing, 0, 0);
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == StdAttr.FACING) {
			instance.recomputeBounds();
			computeTextField(instance);
		} else if (attr == Io.ATTR_LABEL_LOC) {
			computeTextField(instance);
		}
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		// draw
		Bounds bds = painter.getBounds();
		int x = bds.getX(); // x position
		int y = bds.getY(); // y position
		int w = bds.getWidth(); // width
		int h = bds.getHeight(); // height
		int circle = 4; // 0 symbol radius
		int[] xp;
		int[] yp;
		int[] xr;
		int[] yr;
		Object facing = painter.getAttributeValue(StdAttr.FACING);
		Value val;
		if (painter.getShowState()) {
			InstanceDataSingleton data = (InstanceDataSingleton) painter.getData();
			val = data == null ? Value.FALSE : (Value) data.getValue();
		} else {
			val = Value.FALSE;
		}

		Color color = painter.getAttributeValue(Io.ATTR_COLOR);
		if (!painter.shouldDrawColor()) {
			int hue = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
			color = new Color(hue, hue, hue);
		}

		Graphics g = painter.getGraphics();
		int depress;
		if (val == Value.TRUE) { // case true output
			Object labelLoc = painter.getAttributeValue(Io.ATTR_LABEL_LOC);
			if (labelLoc == Io.LABEL_CENTER || labelLoc == Direction.NORTH || labelLoc == Direction.WEST) {
				depress = DEPTH;
			} else {
				depress = 0;
			}

			if (facing == Direction.NORTH || facing == Direction.WEST) {
				Location p = painter.getLocation();
				int px = p.getX();
				int py = p.getY();
				GraphicsUtil.switchToWidth(g, Wire.WIDTH);
				g.setColor(Value.TRUE_COLOR);
				if (facing == Direction.NORTH)
					g.drawLine(px, py, px, py + 10);
				else
					g.drawLine(px, py, px + 10, py);
				GraphicsUtil.switchToWidth(g, 1);
			}

			if (facing == Direction.NORTH || facing == Direction.SOUTH) {// horizontal
				xp = new int[] { x, x + w - DEPTH, x + w, x + w, x };// grey
																		// polygon
																		// x
																		// points
				yp = new int[] { y + DEPTH, y, y + DEPTH, y + h, y + h }; // grey
																			// poligon
																			// y
																			// points
				xr = new int[] { x, x + w - DEPTH, x + w - DEPTH, x }; // white
																		// polygon
																		// x
																		// points
				yr = new int[] { y + DEPTH, y, y + h - DEPTH, y + h }; // white
																		// poligon
																		// y
																		// points

			} else {// vertical
				xp = new int[] { x + DEPTH, x + w, x + w, x + DEPTH, x };
				yp = new int[] { y, y, y + h, y + h, y + DEPTH };
				xr = new int[] { x, x + w - DEPTH, x + w, x + DEPTH };
				yr = new int[] { y + DEPTH, y + DEPTH, y + h, y + h };
			}
			g.setColor(color.darker());
			g.fillPolygon(xp, yp, xp.length);
			g.setColor(color);
			g.fillPolygon(xr, yr, xr.length);
			g.setColor(Color.BLACK);
			g.drawPolygon(xp, yp, xp.length);
			g.drawPolygon(xr, yr, xr.length);
			if (facing == Direction.NORTH || facing == Direction.SOUTH) {
				g.drawLine(x + ((w - DEPTH) / 2), y + (DEPTH / 2), x + ((w - DEPTH) / 2), y + h - (DEPTH / 2));
				g.drawLine(x + w - DEPTH, y + h - DEPTH, x + w, y + h);
				g.drawLine(x + ((w - DEPTH) / 6), y + ((h - DEPTH) / 2) + (DEPTH - DEPTH / 6), x + ((w - DEPTH) / 3),
						y + ((h - DEPTH) / 2) + (DEPTH - DEPTH / 3));
				g.drawOval(x + ((w - DEPTH) * 3 / 4) - (circle / 2), y + ((h - DEPTH) / 2 - (circle / 2)) + (DEPTH / 4),
						circle, circle);
			} else {
				g.drawLine(x + w - DEPTH, y + DEPTH, x + w, y);
				g.drawLine(x + (DEPTH / 2), y + ((h - DEPTH) / 2) + DEPTH, x + w - (DEPTH / 2),
						y + ((h - DEPTH) / 2) + DEPTH);
				g.drawLine(x + ((w - DEPTH) / 2) + (DEPTH - DEPTH / 6), y + ((h - DEPTH) * 5 / 6) + DEPTH,
						x + ((w - DEPTH) / 2) + (DEPTH - DEPTH / 3), y + ((h - DEPTH) * 2 / 3) + DEPTH);
				g.drawOval(x + (DEPTH / 4) + ((w - DEPTH - circle) / 2), y + ((h - DEPTH) / 4 - (circle / 2)) + DEPTH,
						circle, circle);
			}
		} else { // csse false output
			depress = 0;
			if (facing == Direction.NORTH || facing == Direction.SOUTH) {
				xp = new int[] { x, x + DEPTH, x + w, x + w, x };
				yp = new int[] { y + DEPTH, y, y + DEPTH, y + h, y + h };
				xr = new int[] { x + DEPTH, x + w, x + w, x + DEPTH };
				yr = new int[] { y, y + DEPTH, y + h, y + h - DEPTH };
			} else {
				xp = new int[] { x + DEPTH, x + w, x + w, x + DEPTH, x };
				yp = new int[] { y, y, y + h, y + h, y + h - DEPTH };
				xr = new int[] { x + DEPTH, x + w, x + w - DEPTH, x };
				yr = new int[] { y, y, y + h - DEPTH, y + h - DEPTH };
			}
			g.setColor(color.darker());
			g.fillPolygon(xp, yp, xp.length);
			g.setColor(color);
			g.fillPolygon(xr, yr, xr.length);
			g.setColor(Color.BLACK);
			g.drawPolygon(xp, yp, xp.length);
			g.drawPolygon(xr, yr, xr.length);
			if (facing == Direction.NORTH || facing == Direction.SOUTH) {
				g.drawLine(x + DEPTH, y + h - DEPTH, x, y + h);
				g.drawLine(x + ((w - DEPTH) / 2) + DEPTH, y + (DEPTH / 2), x + ((w - DEPTH) / 2) + DEPTH,
						y + h - (DEPTH / 2));
				g.drawLine(x + DEPTH + (w / 6), y + ((h - DEPTH) / 2) + (DEPTH / 6), x + DEPTH + (w / 3),
						y + ((h - DEPTH) / 2) + (DEPTH / 3));
				g.drawOval(x + ((w - DEPTH) * 3 / 4) - (circle / 2) + DEPTH,
						y + ((h - DEPTH) / 2 - (circle / 2)) + (DEPTH * 3 / 4), circle, circle);
			} else {
				g.drawLine(x + (DEPTH / 2), y + ((h - DEPTH) / 2), x + w - (DEPTH / 2), y + ((h - DEPTH) / 2));
				g.drawLine(x + w - DEPTH, y + h - DEPTH, x + w, y + h);
				g.drawLine(x + ((w - DEPTH) / 2) + (DEPTH / 6), y + ((h - DEPTH) * 5 / 6),
						x + ((w - DEPTH) / 2) + (DEPTH / 3), y + ((h - DEPTH) * 2 / 3));
				g.drawOval(x + (DEPTH * 3 / 4) + ((w - DEPTH - circle) / 2), y + ((h - DEPTH) / 4 - (circle / 2)),
						circle, circle);
			}
		}

		g.translate(depress, depress);
		g.setColor(painter.getAttributeValue(Io.ATTR_LABEL_COLOR));
		painter.drawLabel();
		g.translate(-depress, -depress);
		painter.drawPorts();
	}

	@Override
	public void propagate(InstanceState state) {
		InstanceDataSingleton data = (InstanceDataSingleton) state.getData();
		Value val = data == null ? Value.FALSE : (Value) data.getValue();
		state.setPort(0, val, 1);
	}
}
