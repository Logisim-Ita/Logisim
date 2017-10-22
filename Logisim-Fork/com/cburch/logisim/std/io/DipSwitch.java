package com.cburch.logisim.std.io;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstanceLogger;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.GraphicsUtil;

public class DipSwitch extends InstanceFactory {
	public static class Logger extends InstanceLogger {
		@Override
		public String getLogName(InstanceState state, Object option) {
			return state.getAttributeValue(StdAttr.LABEL);
		}

		@Override
		public Value getLogValue(InstanceState state, Object option) {
			return state.getPort(0);
		}
	}

	private class pinValues implements InstanceData, Cloneable {
		Value[] vals = null;

		private pinValues(InstanceState state) {
			vals = new Value[state.getAttributeValue(ATTR_NSWITCHES)];
			for (int i = 0; i < vals.length; i++)
				vals[i] = Value.FALSE;
			state.setData(this);
		}

		@Override
		public Object clone() {
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}

		private Value getValue(int i) {
			if (vals == null)
				return Value.FALSE;
			else
				return vals[i];

		}

		private Value[] getValues() {
			return vals;
		}

		private pinValues getValueState(InstanceState state) {
			int switches = state.getAttributeValue(ATTR_NSWITCHES).intValue();
			pinValues ret = (pinValues) state.getData();
			if (ret == null) {
				ret = new pinValues(state);
				state.setData(ret);
			} else {
				ret.updateValues(state);
			}
			return ret;
		}

		private void setValue(InstanceState state, Value val, int port) {
			vals[port] = val;
			state.getInstance().fireInvalidated();
		}

		private void updateValues(InstanceState state) {
			int switches = state.getAttributeValue(ATTR_NSWITCHES).intValue();
			pinValues obj = (pinValues) state.getData();
			Value[] vals = getValues();
			if (vals.length != switches) {
				if (vals.length < switches) {
					Value[] oldvals = vals;
					vals = new Value[state.getAttributeValue(ATTR_NSWITCHES)];
					int i = 0;
					for (i = 0; i < oldvals.length; i++) {
						vals[i] = oldvals[i];
					}
					for (; i < vals.length; i++) {
						vals[i] = Value.FALSE;
					}
				}
				if (vals.length > switches) {
					Value[] oldvals = vals;
					vals = new Value[state.getAttributeValue(ATTR_NSWITCHES)];
					for (int i = 0; i < switches; i++) {
						vals[i] = oldvals[i];
					}
				}
			}
		}

	}

	public static class Poker extends InstancePoker {
		@Override
		public void mouseReleased(InstanceState state, MouseEvent e) {
			pinValues obj = (pinValues) state.getData();
			Location loc = state.getInstance().getLocation();
			int cx = loc.getX();
			int cy = loc.getY();
			int switches = state.getAttributeValue(ATTR_NSWITCHES);
			Direction dir = state.getAttributeValue(StdAttr.FACING);
			int offset = switches * 10;
			int gx = e.getX();
			int gy = e.getY();
			int x = gx - cx;
			int y = gy - cy;
			if ((y > 5 && y < 25) && (dir == Direction.SOUTH)) {
				x += offset - 5;
				x = x / 10;
				if (x % 2 == 0) {
					x = x / 2;
					obj.setValue(state, state.getPort(x).not(), x);// opposite value
				}
			} else if ((x > 5 && x < 25) && (dir == Direction.EAST)) {
				y += offset - 5;
				y = y / 10;
				if (y % 2 == 0) {
					y = y / 2;
					obj.setValue(state, state.getPort(y).not(), y);// opposite value
				}
			} else if ((y < -5 && y > -25) && (dir == Direction.NORTH)) {
				x += offset - 5;
				x = x / 10;
				if (x % 2 == 0) {
					x = x / 2;
					obj.setValue(state, state.getPort(x).not(), x);// opposite value
				}
			} else if ((x < -5 && x > -25) && (dir == Direction.WEST)) {
				y += offset - 5;
				y /= 10;
				if (y % 2 == 0) {
					y /= 2;
					obj.setValue(state, state.getPort(y).not(), y);// opposite value
				}
			}
		}
	}

	private static final Attribute<Integer> ATTR_NSWITCHES = Attributes.forIntegerRange("NSwitches",
			Strings.getter("NumberOfSwitch"), 1, 32);

	private static final int DEPTH = 3;

	public DipSwitch() {
		super("DipSwitch", Strings.getter("DipSwitchComponent"));
		setAttributes(
				new Attribute[] { StdAttr.FACING, ATTR_NSWITCHES, Io.ATTR_COLOR, StdAttr.LABEL, Io.ATTR_LABEL_LOC,
						StdAttr.LABEL_FONT, Io.ATTR_LABEL_COLOR },
				new Object[] { Direction.EAST, 4, Color.WHITE, "", Direction.WEST, StdAttr.DEFAULT_LABEL_FONT,
						Color.BLACK });
		setFacingAttribute(StdAttr.FACING);
		setIconName("deepswitch.gif");
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
		updateports(instance);
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		Direction facing = attrs.getValue(StdAttr.FACING);
		int y = attrs.getValue(ATTR_NSWITCHES).intValue() * 20;
		return Bounds.create(0, -y / 2, 30, y).rotate(Direction.EAST, facing, 0, 0);
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		//pinValues obj=(pinValues) instance.getData();
		// pinValues obj=(pinValues) instance.getData();
		if (attr == StdAttr.FACING || attr == ATTR_NSWITCHES) {
			instance.recomputeBounds();
			computeTextField(instance);
			updateports(instance);
			//obj.updateValues(obj.getValueState(instance));
			// obj.updateValues(obj.getValueState(instance));
		} else if (attr == Io.ATTR_LABEL_LOC) {
			computeTextField(instance);
		}
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		pinValues obj = (pinValues) painter.getData();
		Bounds bds = painter.getBounds();
		Direction dir = painter.getAttributeValue(StdAttr.FACING);
		painter.drawRoundBounds();

		int x = bds.getX();
		int y = bds.getY();
		int switches = painter.getAttributeValue(ATTR_NSWITCHES).intValue();
		Graphics g = painter.getGraphics();
		GraphicsUtil.switchToWidth(g, 2);
		if (dir == Direction.EAST) {
			g.setColor(Color.BLACK);
			for (int i = 1; i < switches; i++) {
				g.drawLine(0 + x, i * 20 + y, 30 + x, i * 20 + y);

			}
			for (int i = 0; i < switches; i++) {
				// Value val=obj.getValue(i);
				g.setColor(Color.BLACK);
				g.fillRect(5 + x, i * 20 + 5 + y, 20, 10);
				g.setColor(Color.GRAY);
				g.drawRect(15 + x, i * 20 + 5 + y, 10, 10);
			}
		} else if (dir == Direction.WEST) {
			g.setColor(Color.BLACK);
			for (int i = 1; i < switches; i++) {
				g.drawLine(0 + x, i * 20 + y, 30 + x, i * 20 + y);

			}
			for (int i = 0; i < switches; i++) {
				g.setColor(Color.BLACK);
				g.fillRect(5 + x, i * 20 + 5 + y, 20, 10);
				g.setColor(Color.GRAY);
				g.drawRect(15 + x, i * 20 + 5 + y, 10, 10);
			}
		} else if (dir == Direction.NORTH) {
			g.setColor(Color.BLACK);
			for (int i = 1; i < switches; i++) {
				g.drawLine(i * 20 + x, 0 + y, i * 20 + x, 30 + y);

			}
			for (int i = 0; i < switches; i++) {
				g.setColor(Color.BLACK);
				g.fillRect(i * 20 + 5 + x, 5 + y, 10, 20);
				g.setColor(Color.GRAY);
				g.drawRect(i * 20 + 5 + x, 15 + y, 10, 10);
			}
		} else if (dir == Direction.SOUTH) {
			g.setColor(Color.BLACK);
			for (int i = 1; i < switches; i++) {
				g.drawLine(i * 20 + x, 30 + y, i * 20 + x, 0 + y);

			}
			for (int i = 0; i < switches; i++) {
				g.setColor(Color.BLACK);
				g.fillRect(i * 20 + 5 + x, 5 + y, 10, 20);
				g.setColor(Color.GRAY);
				g.drawRect(i * 20 + 5 + x, 15 + y, 10, 10);
			}
		}
		painter.drawLabel();
		painter.drawPorts();
	}

	@Override
	public void propagate(InstanceState state) {
		pinValues obj;
		if (state.getData() != null)
			obj = (pinValues) state.getData();
		else
			obj = new pinValues(state);
		Value val = null;
		for (int i = 0; i < state.getAttributeValue(ATTR_NSWITCHES); i++) {
			val = obj.getValue(i);
			state.setPort(i, val, 1);
		}
	}

	private void updateports(Instance instance) {
		Bounds bds = instance.getBounds();
		Direction dir = instance.getAttributeValue(StdAttr.FACING);
		int offset = (dir == Direction.EAST || dir == Direction.WEST ? bds.getHeight() / 2 : bds.getWidth() / 2);
		int switches = instance.getAttributeValue(ATTR_NSWITCHES).intValue();
		Port[] ports = new Port[switches];

		if (dir == Direction.EAST) {
			for (int i = 0; i < ports.length; i++)
				ports[i] = new Port(30, 20 * i + 10 - offset, Port.OUTPUT, 1);
		} else if (dir == Direction.WEST) {
			for (int i = 0; i < ports.length; i++)
				ports[i] = new Port(-30, 20 * i + 10 - offset, Port.OUTPUT, 1);
		} else if (dir == Direction.NORTH) {
			for (int i = 0; i < ports.length; i++)
				ports[i] = new Port(20 * i + 10 - offset, -30, Port.OUTPUT, 1);
		} else if (dir == Direction.SOUTH) {
			for (int i = 0; i < ports.length; i++)
				ports[i] = new Port(20 * i + 10 - offset, 30, Port.OUTPUT, 1);
		}
		instance.setPorts(ports);
	}

}
