/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.wiring;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.RadixOption;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
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

public class Clock extends InstanceFactory {
	public static class ClockLogger extends InstanceLogger {
		@Override
		public String getLogName(InstanceState state, Object option) {
			return state.getAttributeValue(StdAttr.LABEL);
		}

		@Override
		public Value getLogValue(InstanceState state, Object option) {
			ClockState s = getState(state);
			return s.sending;
		}
	}

	public static class ClockPoker extends InstancePoker {
		boolean isPressed = true;

		private boolean isInside(InstanceState state, MouseEvent e) {
			Bounds bds = state.getInstance().getBounds();
			return bds.contains(e.getX(), e.getY());
		}

		@Override
		public void mousePressed(InstanceState state, MouseEvent e) {
			isPressed = isInside(state, e);
		}

		@Override
		public void mouseReleased(InstanceState state, MouseEvent e) {
			if (isPressed && isInside(state, e)) {
				ClockState myState = (ClockState) state.getData();
				myState.sending = myState.sending.not();
				myState.clicks++;
				state.fireInvalidated();
			}
			isPressed = false;
		}
	}

	private static class ClockState implements InstanceData, Cloneable {
		Value sending = Value.FALSE;
		int clicks = 0;

		@Override
		public ClockState clone() {
			try {
				return (ClockState) super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
	}

	public static final Attribute<Integer> ATTR_HIGH = new DurationAttribute("highDuration",
			Strings.getter("clockHighAttr"), 1, Integer.MAX_VALUE);

	public static final Attribute<Integer> ATTR_LOW = new DurationAttribute("lowDuration",
			Strings.getter("clockLowAttr"), 1, Integer.MAX_VALUE);

	public static final Clock FACTORY = new Clock();

	private static ClockState getState(InstanceState state) {
		ClockState ret = (ClockState) state.getData();
		if (ret == null) {
			ret = new ClockState();
			state.setData(ret);
		}
		return ret;
	}

	//
	// package methods
	//
	public static boolean tick(CircuitState circState, int ticks, Component comp) {
		AttributeSet attrs = comp.getAttributeSet();
		int durationHigh = attrs.getValue(ATTR_HIGH).intValue();
		int durationLow = attrs.getValue(ATTR_LOW).intValue();
		ClockState state = (ClockState) circState.getData(comp);
		if (state == null) {
			state = new ClockState();
			circState.setData(comp, state);
		}
		boolean curValue = ticks % (durationHigh + durationLow) < durationLow;
		if (state.clicks % 2 == 1)
			curValue = !curValue;
		Value desired = (curValue ? Value.FALSE : Value.TRUE);
		if (!state.sending.equals(desired)) {
			state.sending = desired;
			Instance.getInstanceFor(comp).fireInvalidated();
			return true;
		} else {
			return false;
		}
	}

	public Clock() {
		super("Clock", Strings.getter("clockComponent"));
		setAttributes(
				new Attribute[] { StdAttr.FACING, ATTR_HIGH, ATTR_LOW, StdAttr.LABEL, Pin.ATTR_LABEL_LOC,
						StdAttr.LABEL_FONT, StdAttr.ATTR_LABEL_COLOR },
				new Object[] { Direction.EAST, Integer.valueOf(1), Integer.valueOf(1), "", Direction.WEST,
						StdAttr.DEFAULT_LABEL_FONT, Color.BLACK });
		setFacingAttribute(StdAttr.FACING);
		setInstanceLogger(ClockLogger.class);
		setInstancePoker(ClockPoker.class);
		setIconName("clock.gif");
	}

	//
	// private methods
	//
	private void configureLabel(Instance instance) {
		Direction facing = instance.getAttributeValue(StdAttr.FACING);
		Direction labelLoc = instance.getAttributeValue(Pin.ATTR_LABEL_LOC);
		Probe.configureLabel(instance, labelLoc, facing);
	}

	//
	// methods for instances
	//
	@Override
	protected void configureNewInstance(Instance instance) {
		instance.addAttributeListener();
		instance.setPorts(new Port[] { new Port(0, 0, Port.OUTPUT, BitWidth.ONE) });
		configureLabel(instance);
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		return Probe.getOffsetBounds(attrs.getValue(StdAttr.FACING), BitWidth.ONE, RadixOption.RADIX_2);
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == Pin.ATTR_LABEL_LOC) {
			configureLabel(instance);
		} else if (attr == StdAttr.FACING) {
			instance.recomputeBounds();
			configureLabel(instance);
		}
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		Graphics g = painter.getGraphics();
		Bounds bds = painter.getInstance().getBounds();
		int x = bds.getX();
		int y = bds.getY();
		painter.drawRoundBounds(Color.WHITE);
		GraphicsUtil.switchToWidth(g, 2);
		g.setColor(painter.getAttributeValue(StdAttr.ATTR_LABEL_COLOR));
		painter.drawLabel();

		boolean drawUp;
		if (painter.getShowState()) {
			ClockState state = getState(painter);
			g.setColor(state.sending.getColor());
			drawUp = state.sending == Value.TRUE;
		} else {
			g.setColor(Color.BLACK);
			drawUp = true;
		}
		x += 10;
		y += 10;
		int[] xs = { x - 6, x - 6, x, x, x + 6, x + 6 };
		int[] ys;
		if (drawUp) {
			ys = new int[] { y, y - 4, y - 4, y + 4, y + 4, y };
		} else {
			ys = new int[] { y, y + 4, y + 4, y - 4, y - 4, y };
		}
		g.drawPolyline(xs, ys, xs.length);

		painter.drawPorts();
	}

	@Override
	public void propagate(InstanceState state) {
		Value val = state.getPort(0);
		ClockState q = getState(state);
		if (!val.equals(q.sending)) { // ignore if no change
			state.setPort(0, q.sending, 1);
		}
	}
}
