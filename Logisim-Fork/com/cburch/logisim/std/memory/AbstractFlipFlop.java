/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
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
import com.cburch.logisim.util.StringGetter;

abstract class AbstractFlipFlop extends InstanceFactory {
	public static class Logger extends InstanceLogger {
		@Override
		public String getLogName(InstanceState state, Object option) {
			String ret = state.getAttributeValue(StdAttr.LABEL);
			return ret != null && !ret.equals("") ? ret : null;
		}

		@Override
		public Value getLogValue(InstanceState state, Object option) {
			StateData s = (StateData) state.getData();
			return s == null ? Value.FALSE : s.curValue;
		}
	}

	public static class Poker extends InstancePoker {
		boolean isPressed = true;

		private boolean isInside(InstanceState state, MouseEvent e) {
			Location loc = state.getInstance().getLocation();
			int dx = e.getX() - (loc.getX() - 20);
			int dy = e.getY() - (loc.getY() + 10);
			int d2 = dx * dx + dy * dy;
			return d2 < 8 * 8;
		}

		@Override
		public void mousePressed(InstanceState state, MouseEvent e) {
			isPressed = isInside(state, e);
		}

		@Override
		public void mouseReleased(InstanceState state, MouseEvent e) {
			if (isPressed && isInside(state, e)) {
				StateData myState = (StateData) state.getData();
				if (myState == null)
					return;

				myState.curValue = myState.curValue.not();
				state.fireInvalidated();
			}
			isPressed = false;
		}
	}

	private static class StateData extends ClockState implements InstanceData {
		Value curValue = Value.FALSE;
	}

	private static final int STD_PORTS = 5;
	private int inputs;
	private Attribute<AttributeOption> triggerAttribute;

	protected AbstractFlipFlop(String name, String iconName, StringGetter desc, int numInputs,
			boolean allowLevelTriggers) {
		super(name, desc);
		setIconName(iconName);
		triggerAttribute = allowLevelTriggers ? StdAttr.FULL_TRIGGER : StdAttr.EDGE_TRIGGER;
		setAttributes(new Attribute[] { triggerAttribute, StdAttr.LABEL, StdAttr.LABEL_FONT },
				new Object[] { StdAttr.TRIG_RISING, "", StdAttr.DEFAULT_LABEL_FONT });
		setOffsetBounds(Bounds.create(-40, -10, 40, 40));
		setInstancePoker(Poker.class);
		setInstanceLogger(Logger.class);
		inputs = numInputs;
	}

	protected abstract Value computeValue(Value[] inputs, Value curValue);

	//
	// concrete methods not intended to be overridden
	//
	@Override
	protected void configureNewInstance(Instance instance) {
		instance.addAttributeListener();
		Bounds bds = instance.getBounds();
		updateports(instance);
		instance.setTextField(StdAttr.LABEL, StdAttr.LABEL_FONT, bds.getX() + bds.getWidth() / 2, bds.getY() - 3,
				GraphicsUtil.H_CENTER, GraphicsUtil.V_BASELINE);
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == triggerAttribute) {
			updateports(instance);
			instance.recomputeBounds();
		}
	}

	//
	// abstract methods intended to be implemented in subclasses
	//
	protected abstract String getInputName(int index);

	@Override
	public void paintInstance(InstancePainter painter) {
		Graphics g = painter.getGraphics();
		painter.drawBounds();
		painter.drawLabel();
		if (painter.getShowState()) {
			Location loc = painter.getLocation();
			StateData myState = (StateData) painter.getData();
			if (myState != null) {
				int x = loc.getX();
				int y = loc.getY();
				g.setColor(myState.curValue.getColor());
				g.fillOval(x - 26, y + 4, 13, 13);
				g.setColor(Color.WHITE);
				GraphicsUtil.drawCenteredText(g, myState.curValue.toDisplayString(), x - 19, y + 9);
				g.setColor(Color.BLACK);
			}
		}

		int n = inputs;
		g.setColor(Color.GRAY);
		painter.drawPort(n + 2, "0", Direction.SOUTH);
		painter.drawPort(n + 3, "1", Direction.SOUTH);
		painter.drawPort(n + 4, Strings.get("memEnableLabel"), Direction.SOUTH);
		g.setColor(Color.BLACK);
		painter.drawPort(n, "Q", Direction.WEST);
		painter.drawPort(n + 1);
		if (painter.getAttributeValue(triggerAttribute) != StdAttr.TRIG_LATCH)
			painter.drawClock(n + STD_PORTS, Direction.EAST);
		for (int i = 0; i < n; i++)
			painter.drawPort(i, getInputName(i), Direction.EAST);
	}

	@Override
	public void propagate(InstanceState state) {
		StateData data = (StateData) state.getData();
		if (data == null) {
			data = new StateData();
			state.setData(data);
		}

		int n = inputs;
		int withclock = state.getAttributeValue(triggerAttribute) == StdAttr.TRIG_LATCH ? 0 : 1;
		Object triggerType = state.getAttributeValue(triggerAttribute);
		boolean triggered = (withclock == 1) ? data.updateClock(state.getPort(n + STD_PORTS), triggerType) : true;

		if (state.getPort(n + 2) == Value.TRUE) { // clear requested
			data.curValue = Value.FALSE;
		} else if (state.getPort(n + 3) == Value.TRUE) { // preset requested
			data.curValue = Value.TRUE;
		} else if (triggered && state.getPort(n + 4) != Value.FALSE) {
			// Clock has triggered and flip-flop is enabled: Update the state
			Value[] inputs = new Value[n];
			for (int i = 0; i < n; i++) {
				inputs[i] = state.getPort(i);
			}

			Value newVal = computeValue(inputs, data.curValue);
			if (newVal == Value.TRUE || newVal == Value.FALSE) {
				data.curValue = newVal;
			}
		}

		state.setPort(n, data.curValue, Memory.DELAY);
		state.setPort(n + 1, data.curValue.not(), Memory.DELAY);
	}

	private void updateports(Instance instance) {
		int numInputs = inputs;
		int withclock = instance.getAttributeValue(triggerAttribute) == StdAttr.TRIG_LATCH ? 0 : 1;
		Port[] ps = new Port[numInputs + STD_PORTS + withclock];
		if (numInputs == 1)
			ps[0] = new Port(-40, 20, Port.INPUT, 1);
		else if (numInputs == 2) {
			ps[0] = new Port(-40, 0, Port.INPUT, 1);
			ps[1] = new Port(-40, 20, Port.INPUT, 1);
		} else {
			throw new RuntimeException("flip-flop input > 2");
		}

		if (instance.getAttributeValue(triggerAttribute) != StdAttr.TRIG_LATCH) {
			ps[numInputs + STD_PORTS] = new Port(-40, 10 * (numInputs - 1), Port.INPUT, 1);
			ps[numInputs + STD_PORTS].setToolTip(Strings.getter("flipFlopClockTip"));
		}
		ps[numInputs] = new Port(0, 0, Port.OUTPUT, 1); // Q
		ps[numInputs + 1] = new Port(0, 20, Port.OUTPUT, 1); // Qn
		ps[numInputs + 2] = new Port(-10, 30, Port.INPUT, 1); // Clear
		ps[numInputs + 3] = new Port(-30, 30, Port.INPUT, 1); // Preset
		ps[numInputs + 4] = new Port(-20, 30, Port.INPUT, 1); // Enable
		ps[numInputs].setToolTip(Strings.getter("flipFlopQTip"));
		ps[numInputs + 1].setToolTip(Strings.getter("flipFlopNotQTip"));
		ps[numInputs + 2].setToolTip(Strings.getter("flipFlopResetTip"));
		ps[numInputs + 3].setToolTip(Strings.getter("flipFlopPresetTip"));
		ps[numInputs + 4].setToolTip(Strings.getter("flipFlopEnableTip"));
		instance.setPorts(ps);
	}
}