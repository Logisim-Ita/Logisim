/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringUtil;

public class Counter extends InstanceFactory {
	static final AttributeOption ON_GOAL_WRAP = new AttributeOption("wrap", "wrap", Strings.getter("counterGoalWrap"));
	static final AttributeOption ON_GOAL_STAY = new AttributeOption("stay", "stay", Strings.getter("counterGoalStay"));
	static final AttributeOption ON_GOAL_CONT = new AttributeOption("continue", "continue",
			Strings.getter("counterGoalContinue"));
	static final AttributeOption ON_GOAL_LOAD = new AttributeOption("load", "load", Strings.getter("counterGoalLoad"));

	static final Attribute<Integer> ATTR_MAX = Attributes.forHexInteger("max", Strings.getter("counterMaxAttr"));
	static final Attribute<AttributeOption> ATTR_ON_GOAL = Attributes.forOption("ongoal",
			Strings.getter("counterGoalAttr"),
			new AttributeOption[] { ON_GOAL_WRAP, ON_GOAL_STAY, ON_GOAL_CONT, ON_GOAL_LOAD });
	static final AttributeOption NEW_BEHAVIOR = new AttributeOption("new", "new", Strings.getter("counterNewBehavior"));
	static final AttributeOption OLD_BEHAVIOR = new AttributeOption("old", "old", Strings.getter("counterOldBehavior"));
	static final Attribute<AttributeOption> BEHAVIOR = Attributes.forOption("behavior",
			Strings.getter("counterBehavior"), new AttributeOption[] { NEW_BEHAVIOR, OLD_BEHAVIOR });

	private static final byte DELAY = 8;
	private static final byte OUT = 0;
	private static final byte IN = 1;
	private static final byte CK = 2;
	private static final byte CLR = 3;
	private static final byte LD = 4;
	private static final byte CT = 5;
	private static final byte CARRY = 6;
	private static final byte PRE = 7;
	private static final byte EN = 8;

	public Counter() {
		super("Counter", Strings.getter("counterComponent"));
		setOffsetBounds(Bounds.create(-30, -20, 30, 40));
		setIconName("counter.gif");
		setInstancePoker(RegisterPoker.class);
		setInstanceLogger(RegisterLogger.class);
		setKeyConfigurator(new BitWidthConfigurator(StdAttr.WIDTH));
	}

	@Override
	protected void configureNewInstance(Instance instance) {
		updateports(instance);
		Bounds bds = instance.getBounds();
		instance.addAttributeListener();
		instance.setTextField(StdAttr.LABEL, StdAttr.LABEL_FONT, StdAttr.ATTR_LABEL_COLOR,
				bds.getX() + bds.getWidth() / 2, bds.getY() - 3, GraphicsUtil.H_CENTER, GraphicsUtil.V_BOTTOM);
	}

	@Override
	public AttributeSet createAttributeSet() {
		return new CounterAttributes();
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == BEHAVIOR) {
			updateports(instance);
			instance.fireInvalidated();
		}
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		Graphics g = painter.getGraphics();
		Bounds bds = painter.getBounds();
		RegisterData state = (RegisterData) painter.getData();
		BitWidth widthVal = painter.getAttributeValue(StdAttr.WIDTH);
		int width = widthVal == null ? 8 : widthVal.getWidth();

		// determine text to draw in label
		String a;
		String b = null;
		if (painter.getShowState()) {
			int val = state == null ? 0 : state.value;
			String str = StringUtil.toHexString(width, val);
			if (str.length() <= 4) {
				a = str;
			} else {
				int split = str.length() - 4;
				a = str.substring(0, split);
				b = str.substring(split);
			}
		} else {
			a = Strings.get("counterLabel");
			b = Strings.get("registerWidthLabel", "" + widthVal.getWidth());
		}

		// draw boundary, label
		painter.drawRoundBounds(Color.WHITE);
		painter.drawLabel();

		// draw input and output ports
		if (b == null) {
			painter.drawPort(IN, "D", Direction.EAST);
			painter.drawPort(OUT, "Q", Direction.WEST);
		} else {
			painter.drawPort(IN);
			painter.drawPort(OUT);
		}
		g.setColor(Color.GRAY);
		painter.drawPort(LD);
		painter.drawPort(CARRY);
		painter.drawPort(PRE);
		painter.drawPort(CLR, "0", Direction.SOUTH);
		painter.drawPort(CT, Strings.get("counterEnableLabel"), Direction.EAST);
		if (painter.getAttributeValue(BEHAVIOR) == NEW_BEHAVIOR)
			painter.drawPort(EN);
		g.setColor(Color.BLACK);
		painter.drawClock(CK, Direction.NORTH);

		// draw contents
		if (b == null) {
			GraphicsUtil.drawText(g, a, bds.getX() + 15, bds.getY() + 4, GraphicsUtil.H_CENTER, GraphicsUtil.V_TOP);
		} else {
			GraphicsUtil.drawText(g, a, bds.getX() + 15, bds.getY() + 3, GraphicsUtil.H_CENTER, GraphicsUtil.V_TOP);
			GraphicsUtil.drawText(g, b, bds.getX() + 15, bds.getY() + 15, GraphicsUtil.H_CENTER, GraphicsUtil.V_TOP);
		}
	}

	@Override
	public void propagate(InstanceState state) {
		RegisterData data = (RegisterData) state.getData();
		if (data == null) {
			data = new RegisterData();
			state.setData(data);
		}

		BitWidth dataWidth = state.getAttributeValue(StdAttr.WIDTH);
		Object triggerType = state.getAttributeValue(StdAttr.EDGE_TRIGGER);
		int max = state.getAttributeValue(ATTR_MAX).intValue();
		Value clock = state.getPort(CK);
		boolean triggered = data.updateClock(clock, triggerType);
		boolean newbehavior = state.getAttributeValue(BEHAVIOR) == NEW_BEHAVIOR;
		Value newValue;
		boolean carry, ld, ct;
		if (state.getPort(CLR) == Value.TRUE) {
			newValue = Value.createKnown(dataWidth, 0);
			carry = false;
		} else if (state.getPort(PRE) == Value.TRUE) {
			newValue = Value.createKnown(dataWidth, -1);
			carry = false;
		} else {
			if (newbehavior) {
				if (state.getPort(EN) == Value.FALSE)
					return;
				ct = state.getPort(CT) == Value.TRUE;
			} else
				ct = state.getPort(CT) != Value.FALSE;
			ld = state.getPort(LD) == Value.TRUE;
			int oldVal = data.value;
			int newVal;
			if (!triggered) {
				newVal = oldVal;
			} else if (!newbehavior && ct) { // trigger, enable = 1, old behavior: should increment or decrement
				int goal = ld ? 0 : max;
				if (oldVal == goal) {
					Object onGoal = state.getAttributeValue(ATTR_ON_GOAL);
					if (onGoal == ON_GOAL_WRAP) {
						newVal = ld ? max : 0;
					} else if (onGoal == ON_GOAL_STAY) {
						newVal = oldVal;
					} else if (onGoal == ON_GOAL_LOAD) {
						Value in = state.getPort(IN);
						newVal = in.isFullyDefined() ? in.toIntValue() : 0;
						if (newVal > max)
							newVal &= max;
					} else if (onGoal == ON_GOAL_CONT) { // decrement or increment
						newVal = ld ? oldVal - 1 : oldVal + 1;
					} else {
						System.err.println("Invalid goal attribute " + onGoal); // OK
						newVal = ld ? max : 0;
					}
				} else { // decrement or increment
					newVal = ld ? oldVal - 1 : oldVal + 1;
				}
			} else if (ld) { // trigger, !(newbehavior && count), load = 1: should load
				Value in = state.getPort(IN);
				newVal = in.isFullyDefined() ? in.toIntValue() : 0;
				if (newVal > max)
					newVal &= max;
			} else if (newbehavior) { // ld = 0, newbehavior
				int goal = ct ? 0 : max;
				if (oldVal == goal) {
					Object onGoal = state.getAttributeValue(ATTR_ON_GOAL);
					if (onGoal == ON_GOAL_WRAP) {
						newVal = ct ? max : 0;
					} else if (onGoal == ON_GOAL_STAY) {
						newVal = oldVal;
					} else if (onGoal == ON_GOAL_LOAD) {
						Value in = state.getPort(IN);
						newVal = in.isFullyDefined() ? in.toIntValue() : 0;
						if (newVal > max)
							newVal &= max;
					} else if (onGoal == ON_GOAL_CONT) { // decrement or increment
						newVal = ct ? oldVal - 1 : oldVal + 1;
					} else {
						System.err.println("Invalid goal attribute " + onGoal); // OK
						newVal = ct ? max : 0;
					}
				} else { // decrement or increment
					newVal = ct ? oldVal - 1 : oldVal + 1;
				}
			} else { // trigger, enable = 0, load = 0: no change
				newVal = oldVal;
			}
			newValue = Value.createKnown(dataWidth, newVal);
			newVal = newValue.toIntValue();
			carry = newVal == ((!newbehavior && ld && ct) || (newbehavior && ct) ? 0 : max);
			/*
			 * I would want this if I were worried about the carry signal outrunning the
			 * clock. But the component's delay should be enough to take care of it. if
			 * (carry) { if (triggerType == StdAttr.TRIG_FALLING) { carry = clock ==
			 * Value.TRUE; } else { carry = clock == Value.FALSE; } }
			 */
		}

		data.value = newValue.toIntValue();
		state.setPort(OUT, newValue, DELAY);
		state.setPort(CARRY, carry ? Value.TRUE : Value.FALSE, DELAY);
	}

	private void updateports(Instance instance) {
		boolean newbehavior = instance.getAttributeValue(BEHAVIOR) == NEW_BEHAVIOR;
		Port[] ps = new Port[(newbehavior ? 9 : 8)];
		ps[OUT] = new Port(0, 0, Port.OUTPUT, StdAttr.WIDTH);
		ps[IN] = new Port(-30, 0, Port.INPUT, StdAttr.WIDTH);
		ps[CK] = new Port(-20, 20, Port.INPUT, 1);
		ps[CLR] = new Port(-10, 20, Port.INPUT, 1);
		ps[LD] = new Port(-30, -10, Port.INPUT, 1);
		ps[CT] = new Port(-30, 10, Port.INPUT, 1);
		ps[CARRY] = new Port(0, 10, Port.OUTPUT, 1);
		ps[PRE] = new Port(-10, -20, Port.INPUT, 1);
		ps[OUT].setToolTip(Strings.getter("counterQTip"));
		ps[IN].setToolTip(Strings.getter("counterDataTip"));
		ps[CK].setToolTip(Strings.getter("counterClockTip"));
		ps[CLR].setToolTip(Strings.getter("counterResetTip"));
		ps[CARRY].setToolTip(Strings.getter("counterCarryTip"));
		ps[PRE].setToolTip(Strings.getter("registerPreTip"));
		if (newbehavior) {
			ps[EN] = new Port(-20, -20, Port.INPUT, 1);
			ps[LD].setToolTip(Strings.getter("newcounterLoadTip"));
			ps[CT].setToolTip(Strings.getter("newcounterReverseCountTip"));
			ps[EN].setToolTip(Strings.getter("newcounterEnableTip"));

		} else {
			ps[LD].setToolTip(Strings.getter("counterLoadTip"));
			ps[CT].setToolTip(Strings.getter("counterEnableTip"));
		}
		instance.setPorts(ps);
	}
}