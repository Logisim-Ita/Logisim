/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.LogisimVersion;
import com.cburch.logisim.Main;
import com.cburch.logisim.data.Attribute;
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
import com.cburch.logisim.tools.key.IntegerConfigurator;
import com.cburch.logisim.tools.key.JoinedConfigurator;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringGetter;

public class UniversalRegister extends InstanceFactory {
	static final Attribute<Integer> ATTR_LENGTH = Attributes.forIntegerRange("length",
			Strings.getter("universalRegLengthAttr"), 1, 32);

	private static final int IN = 0;
	private static final int CK = 1;
	private static final int CLR = 2;
	private static final int OUT = 3;
	private static final int MODE = 4;

	static final Value HOLD = Value.create(new Value[] { Value.FALSE, Value.FALSE });
	static final Value SHIFT_LEFT = Value.create(new Value[] { Value.FALSE, Value.TRUE });
	static final Value SHIFT_RIGHT = Value.create(new Value[] { Value.TRUE, Value.FALSE });
	static final Value PARALLEL_LOAD = Value.create(new Value[] { Value.TRUE, Value.TRUE });

	public UniversalRegister() {
		super("Universal Register", Strings.getter("universalRegisterComponent"));
		setAttributes(
				new Attribute[] { ATTR_LENGTH, StdAttr.EDGE_TRIGGER, StdAttr.LABEL,
						StdAttr.LABEL_FONT, StdAttr.ATTR_LABEL_COLOR },
				new Object[] { Integer.valueOf(8), StdAttr.TRIG_RISING, "",
						StdAttr.DEFAULT_LABEL_FONT, Color.BLACK });

		setOffsetBounds(Bounds.create(0, -20, 30, 40));
		setIconName("universalreg.gif");
		setInstanceLogger(UniversalRegisterLogger.class);
	}

	@Override
	protected void configureNewInstance(Instance instance) {
		Bounds bds = instance.getBounds();
		configurePorts(instance);
		instance.setTextField(StdAttr.LABEL, StdAttr.LABEL_FONT, StdAttr.ATTR_LABEL_COLOR,
				bds.getX() + bds.getWidth() / 2, bds.getY() - 3, GraphicsUtil.H_CENTER, GraphicsUtil.V_BOTTOM);
		instance.addAttributeListener();
	}

	private void configurePorts(Instance instance) {
		Bounds bds = instance.getBounds();
		Port[] ps = new Port[5];
		Integer lenObj = instance.getAttributeValue(ATTR_LENGTH);
		int len = lenObj == null ? 8 : lenObj.intValue();
		
		ps[IN] = new Port(0, 0, Port.INPUT, len);
		ps[IN].setToolTip(Strings.getter("universalRegInTip"));
		ps[CK] = new Port(0, 10, Port.INPUT, 1);
		ps[CK].setToolTip(Strings.getter("universalRegClockTip"));
		ps[CLR] = new Port(10, 20, Port.INPUT, 1);
		ps[CLR].setToolTip(Strings.getter("universalRegClearTip"));
		ps[OUT] = new Port(bds.getWidth(), 0, Port.OUTPUT, len);
		ps[OUT].setToolTip(Strings.getter("universalRegOutTip"));
		ps[MODE] = new Port(10, -20, Port.INPUT, 2);
		ps[MODE].setToolTip(Strings.getter("universalRegModeTip"));

		instance.setPorts(ps);
	}

	private ShiftRegisterData getData(InstanceState state) {
		Integer lenObj = state.getAttributeValue(ATTR_LENGTH);
		int length = lenObj == null ? 8 : lenObj.intValue();
		ShiftRegisterData data = (ShiftRegisterData) state.getData();
		if (data == null) {
			data = new ShiftRegisterData(length);
			state.setData(data);
		} else {
			data.setDimensions(length);
		}
		return data;
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == ATTR_LENGTH) {
			instance.recomputeBounds();
			configurePorts(instance);
		}
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		// draw boundary, label
		painter.drawRoundBounds(Color.WHITE);

		// draw input and output ports
		int ports = painter.getInstance().getPorts().size();
		for (int i = 0; i < ports; i++) {
			if (i != CK)
				painter.drawPort(i);
		}
		painter.drawClock(CK, Direction.EAST);
		painter.drawLabel();
	}

	@Override
	public void propagate(InstanceState state) {
		Object triggerType = state.getAttributeValue(StdAttr.EDGE_TRIGGER);
		ShiftRegisterData data = getData(state);
		Value mode = state.getPort(MODE);
		boolean triggered = data.updateClock(state.getPort(CK), triggerType);

		if (state.getPort(CLR) == Value.TRUE) {
			data.clear();
		} else if (triggered) {
			if (mode.equals(HOLD)) {
				return;
			} else if (mode.equals(PARALLEL_LOAD)) {
				data.parallelLoad(state.getPort(IN).getAll());
			} else {
				data.push(mode, mode.equals(SHIFT_RIGHT) ? state.getPort(IN).get(data.getLength() - 1) : state.getPort(IN).get(0));
			}
		}

		state.setPort(OUT, data.getValues(), 1);
	}
}