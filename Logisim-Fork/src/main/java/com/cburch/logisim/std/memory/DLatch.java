package com.cburch.logisim.std.memory;

import com.cburch.logisim.data.Value;

public class DLatch extends AbstractFlipFlop {

	public DLatch() {
		super("D Latch", "dFlipFlop.gif", Strings.getter("dLatchComponent"), (byte) 1, true);
	}

	@Override
	protected Value computeValue(Value[] inputs, Value curValue) {
		return inputs[0];
	}

	@Override
	protected String getInputName(int index) {
		return "D";
	}

}
