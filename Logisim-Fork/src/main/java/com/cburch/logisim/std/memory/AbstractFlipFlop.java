/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.Bounds;
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
import com.cburch.logisim.prefs.AppPreferences;
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
			Bounds bds = state.getInstance().getBounds();
			int dx = e.getX() - (bds.getX() + bds.getWidth() / 2);
			int dy = e.getY() - (bds.getY() + bds.getHeight() / 2);
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

	private static final int STD_PORTS = 4;
	// attribute clear preset position
	private static final AttributeOption ABOVE_BELOW = new AttributeOption("ABOVE_BELOW", Strings.getter("AboveBelow"));
	private static final AttributeOption BELOW_ABOVE = new AttributeOption("BELOW_ABOVE", Strings.getter("BelowAbove"));

	private static final AttributeOption LEGACY = new AttributeOption("LEGACY", Strings.getter("Legacy"));
	private static final Attribute<AttributeOption> PRE_CLR_POSITION = Attributes.forOption("Pre/Clr Positions",
			Strings.getter("PresetClearPosition"), new AttributeOption[] { ABOVE_BELOW, BELOW_ABOVE, LEGACY });
	// attribute negate clear preset
	private static final Attribute<Boolean> NEGATE_PRE_CLR = Attributes.forBoolean("NegatePresetClear",
			Strings.getter("NegatePresetClear"));
	private byte inputs;
	private Attribute<AttributeOption> triggerAttribute;

	protected AbstractFlipFlop(String name, String iconName, StringGetter desc, byte numInputs) {
		super(name, desc);
		setIconName(iconName);
		triggerAttribute = StdAttr.EDGE_TRIGGER;
		setAttributes(
				new Attribute[] { triggerAttribute, PRE_CLR_POSITION, NEGATE_PRE_CLR, StdAttr.LABEL,
						StdAttr.LABEL_FONT, StdAttr.ATTR_LABEL_COLOR },
				new Object[] { StdAttr.TRIG_RISING, ABOVE_BELOW, Boolean.FALSE, "", StdAttr.DEFAULT_LABEL_FONT,
						Color.BLACK });

		setInstancePoker(Poker.class);
		setInstanceLogger(Logger.class);
		this.inputs = numInputs;
	}

	protected AbstractFlipFlop(String name, String iconName, StringGetter desc, byte numInputs, boolean onlyEnable) {
		super(name, desc);
		setIconName(iconName);
		triggerAttribute = onlyEnable ? StdAttr.LATCH_TRIGGER_E : StdAttr.FULL_LATCH_TRIGGER;
		setAttributes(
				new Attribute[] { triggerAttribute, PRE_CLR_POSITION, NEGATE_PRE_CLR, StdAttr.LABEL,
						StdAttr.LABEL_FONT, StdAttr.ATTR_LABEL_COLOR },
				new Object[] { StdAttr.TRIG_HIGH, ABOVE_BELOW, Boolean.FALSE, "", StdAttr.DEFAULT_LABEL_FONT,
						Color.BLACK });

		setInstancePoker(Poker.class);
		setInstanceLogger(Logger.class);
		this.inputs = numInputs;
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
		instance.setTextField(StdAttr.LABEL, StdAttr.LABEL_FONT, StdAttr.ATTR_LABEL_COLOR,
				bds.getX() + bds.getWidth() / 2, bds.getY() - 3, GraphicsUtil.H_CENTER, GraphicsUtil.V_BOTTOM);
	}

	//
	// abstract methods intended to be implemented in subclasses
	//
	protected abstract String getInputName(int index);

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		int width = 60;
		int height = 80;
		byte offs = (byte) -20;
		return Bounds.create(-width, offs, width, height);
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == triggerAttribute || attr == PRE_CLR_POSITION || attr == NEGATE_PRE_CLR) {
			instance.recomputeBounds();
			updateports(instance);
		}
	}

	private void paintBase(InstancePainter painter, boolean isGhost) {
		Graphics g = painter.getGraphics();
		Bounds bds = painter.getBounds();
		int n = inputs;
		// 0=above/below,1=below/above,2=legacy
		int prclrpos = painter.getAttributeValue(PRE_CLR_POSITION) == ABOVE_BELOW ? 0
				: painter.getAttributeValue(PRE_CLR_POSITION) == BELOW_ABOVE ? 1 : 2;
		boolean prclrneg = painter.getAttributeValue(NEGATE_PRE_CLR);
		GraphicsUtil.switchToWidth(g, 2);

		if (AppPreferences.FILL_COMPONENT_BACKGROUND.getBoolean() && !isGhost) {
			g.setColor(Color.WHITE);
			g.fillRoundRect(bds.getX() + 10, bds.getY() + 10, bds.getWidth() - 20, bds.getHeight() - 20, 10, 10);
			g.setColor(Color.BLACK);
		}
		g.drawRoundRect(bds.getX() + 10, bds.getY() + 10, bds.getWidth() - 20, bds.getHeight() - 20, 10, 10);

		if (!isGhost) {
			g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
			if (painter.getShowState()) {
				StateData myState = (StateData) painter.getData();
				if (myState != null) {
					int x = bds.getX();
					int y = bds.getY();
					g.setColor(myState.curValue.getColor());
					g.fillOval(x + bds.getWidth() / 2 - 6, y + bds.getHeight() / 2 - 6, 13, 13);
					g.setColor(Color.WHITE);
					GraphicsUtil.drawCenteredText(g, myState.curValue.toDisplayString(), x + bds.getWidth() / 2 + 1,
							y + bds.getHeight() / 2 - 1);
					g.setColor(Color.BLACK);
				}
			}
		}

		// new layout
		if (prclrpos != 2) {
			if (!prclrneg) {
				// upper centered port line (pr/clr)
				g.drawLine(bds.getX() + bds.getWidth() / 2, bds.getY(), bds.getX() + bds.getWidth() / 2,
						bds.getY() + 10);
				// lower centered port line (pr/clr)
				g.drawLine(bds.getX() + bds.getWidth() / 2, bds.getY() + bds.getHeight(),
						bds.getX() + bds.getWidth() / 2, bds.getY() + bds.getHeight() - 10);
			} else {// negated pr/clr
				g.drawOval(bds.getX() + 26, bds.getY() + 2, 8, 8);
				g.drawOval(bds.getX() + 26, bds.getY() + bds.getHeight() - 10, 8, 8);
			}
		} else {// 1st and 3rd lower line if legacy position
			if (!prclrneg) {
				g.drawLine(bds.getX() + 20, bds.getY() + bds.getHeight(), bds.getX() + 20,
						bds.getY() + bds.getHeight() - 10);
				g.drawLine(bds.getX() + 40, bds.getY() + bds.getHeight(), bds.getX() + 40,
						bds.getY() + bds.getHeight() - 10);
			} else {// negated pr/clr
				g.drawOval(bds.getX() + 16, bds.getY() + bds.getHeight() - 10, 8, 8);
				g.drawOval(bds.getX() + 36, bds.getY() + bds.getHeight() - 10, 8, 8);
			}
		}
		if (!isGhost) {
			g.setColor(Color.GRAY);
			GraphicsUtil.drawCenteredText(g, "0",
					prclrpos != 2 ? bds.getX() + bds.getWidth() / 2 : bds.getX() + 40,
					prclrpos != 1 ? bds.getY() + bds.getHeight() - 20 : bds.getY() + 17);
			GraphicsUtil.drawCenteredText(g, "1",
					prclrpos != 2 ? bds.getX() + bds.getWidth() / 2 : bds.getX() + 20,
					prclrpos != 0 ? bds.getY() + bds.getHeight() - 20 : bds.getY() + 17);
			g.setColor(Color.BLACK);
		}
		if (painter.getAttributeValue(triggerAttribute) != StdAttr.TRIG_LATCH) {
			if (painter.getAttributeValue(triggerAttribute) != StdAttr.TRIG_FALLING
					&& painter.getAttributeValue(triggerAttribute) != StdAttr.TRIG_LOW)
				g.drawLine(bds.getX(), bds.getY() + 20, bds.getX() + 10, bds.getY() + 20);
			else
				g.drawOval(bds.getX() + 2, bds.getY() + 16, 8, 8);
			if (triggerAttribute == StdAttr.EDGE_TRIGGER) {
				// Paint ">"
				g.drawLine(bds.getX() + 11, bds.getY() + 16, bds.getX() + 15, bds.getY() + 20);
				g.drawLine(bds.getX() + 11, bds.getY() + 24, bds.getX() + 15, bds.getY() + 20);
			} else {
				// Paint "E"
				GraphicsUtil.drawCenteredText(g, "E", bds.getX() + 17, bds.getY() + 20);
			}
			if (!isGhost)
				painter.drawPort(n + STD_PORTS);
		}
		for (byte i = 0; i < n; i++) {
			// inputs
			g.drawLine(bds.getX(), bds.getY() + (i + 1 == n ? 60 : 40), bds.getX() + 10,
					bds.getY() + (i + 1 == n ? 60 : 40));
			if (!isGhost) {
				GraphicsUtil.drawCenteredText(g, getInputName(i), bds.getX() + 17,
						bds.getY() + (i + 1 == n ? 58 : 40));
				painter.drawPort(i);
			}
		}
		// Qn
		g.drawOval(bds.getX() + bds.getWidth() - 10, bds.getY() + bds.getHeight() - 24, 8, 8);
		// Q
		g.drawLine(bds.getX() + bds.getWidth() - 10, bds.getY() + 20, bds.getX() + bds.getWidth(), bds.getY() + 20);
		if (!isGhost) {
			// Q
			GraphicsUtil.drawCenteredText(g, "Q", bds.getX() + 42, bds.getY() + 17);
			painter.drawPort(n);
			// Qn
			painter.drawPort(n + 1);
			// clear
			painter.drawPort(n + 2);
			// preset
			painter.drawPort(n + 3);
		}

	}

	@Override
	public void paintGhost(InstancePainter painter) {
		paintBase(painter, true);
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		paintBase(painter, false);
		painter.drawLabel();
	}

	@Override
	public void propagate(InstanceState state) {
		StateData data = (StateData) state.getData();
		if (data == null) {
			data = new StateData();
			state.setData(data);
		}

		byte n = inputs;
		byte withclock = (byte) (state.getAttributeValue(triggerAttribute) == StdAttr.TRIG_LATCH ? 0 : 1);
		Object triggerType = state.getAttributeValue(triggerAttribute);
		Value clear = state.getAttributeValue(NEGATE_PRE_CLR) ? state.getPort(n + 2).not() : state.getPort(n + 2);
		Value preset = state.getAttributeValue(NEGATE_PRE_CLR) ? state.getPort(n + 3).not() : state.getPort(n + 3);
		boolean triggered = (withclock == 1) ? data.updateClock(state.getPort(n + STD_PORTS), triggerType)
				: true;

		if (clear == Value.TRUE) { // clear requested
			data.curValue = Value.FALSE;
		} else if (preset == Value.TRUE) { // preset requested
			data.curValue = Value.TRUE;
		} else if (triggered) {
			// Update the state
			Value[] inputs = new Value[n];
			for (byte i = 0; i < n; i++) {
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
		Bounds bds = instance.getBounds();
		// 0=above/below,1=below/above,2=legacy
		byte prclrpos = (byte) (instance.getAttributeValue(PRE_CLR_POSITION) == ABOVE_BELOW ? 0
				: instance.getAttributeValue(PRE_CLR_POSITION) == BELOW_ABOVE ? 1 : 2);
		Boolean isFlipFlop = triggerAttribute == StdAttr.EDGE_TRIGGER;
		byte numInputs = this.inputs;
		byte withclock = (byte) (instance.getAttributeValue(triggerAttribute) == StdAttr.TRIG_LATCH ? 0 : 1);
		Port[] ps = new Port[numInputs + STD_PORTS + withclock];
		if (numInputs == 1)
			ps[0] = new Port(-bds.getWidth(), 40, Port.INPUT, 1);
		else if (numInputs == 2) {
			ps[0] = new Port(-bds.getWidth(), 20, Port.INPUT, 1);
			ps[1] = new Port(-bds.getWidth(), 40, Port.INPUT, 1);
		} else {
			if (triggerAttribute == StdAttr.EDGE_TRIGGER)
				throw new RuntimeException("flip-flop input > 2");
			else
				throw new RuntimeException("latch input > 2");
		}

		if (instance.getAttributeValue(triggerAttribute) != StdAttr.TRIG_LATCH) {
			ps[numInputs + STD_PORTS] = new Port(-bds.getWidth(), 0,
					Port.INPUT, 1);
			ps[numInputs + STD_PORTS]
					.setToolTip(isFlipFlop ? Strings.getter("flipFlopClockTip") : Strings.getter("latchETip"));
		}
		ps[numInputs] = new Port(0, 0, Port.OUTPUT, 1); // Q
		ps[numInputs + 1] = new Port(0, 40, Port.OUTPUT, 1); // Qn
		ps[numInputs + 2] = new Port(prclrpos == 2 ? -bds.getWidth() / 3 : -bds.getWidth() / 2,
				prclrpos == 1 ? -20 : bds.getHeight() - 20, Port.INPUT, 1); // Clear
		ps[numInputs + 3] = new Port(prclrpos == 2 ? -bds.getWidth() * 2 / 3 : -bds.getWidth() / 2,
				prclrpos == 0 ? -20 : bds.getHeight() - 20, Port.INPUT, 1); // Preset
		ps[numInputs].setToolTip(isFlipFlop ? Strings.getter("flipFlopQTip") : Strings.getter("latchQTip"));
		ps[numInputs + 1].setToolTip(isFlipFlop ? Strings.getter("flipFlopNotQTip") : Strings.getter("latchNotQTip"));
		if (instance.getAttributeValue(NEGATE_PRE_CLR)) {
			ps[numInputs + 2].setToolTip(Strings.getter("flipFlopResetTip", "0"));
			ps[numInputs + 3].setToolTip(Strings.getter("flipFlopPresetTip", "0"));
		} else {
			ps[numInputs + 2].setToolTip(Strings.getter("flipFlopResetTip", "1"));
			ps[numInputs + 3].setToolTip(Strings.getter("flipFlopPresetTip", "1"));
		}
		instance.setPorts(ps);
	}
}