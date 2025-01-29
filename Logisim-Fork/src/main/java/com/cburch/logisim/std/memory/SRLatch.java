package com.cburch.logisim.std.memory;

import com.cburch.logisim.data.Value;

public class SRLatch extends AbstractFlipFlop {
	public SRLatch() {
		super("S-R Latch", "srFlipFlop.gif", Strings.getter("srLatchComponent"), (byte) 2, false);
	}

	@Override
	protected Value computeValue(Value[] inputs, Value curValue) {
		if (inputs[0] == Value.FALSE) {
			if (inputs[1] == Value.FALSE) {
				return curValue;
			} else if (inputs[1] == Value.TRUE) {
				return Value.FALSE;
			}
		} else if (inputs[0] == Value.TRUE) {
			if (inputs[1] == Value.FALSE) {
				return Value.TRUE;
			} else if (inputs[1] == Value.TRUE) {
				return Value.ERROR;
			}
		}
		return Value.UNKNOWN;
	}

	@Override
	protected String getInputName(int index) {
		return index == 0 ? "S" : "R";
	}
}
