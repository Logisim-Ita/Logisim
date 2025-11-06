/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import java.util.Arrays;

import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceData;

public class ShiftRegisterData extends ClockState implements InstanceData {
	private Value[] vs;

	public ShiftRegisterData(int len) {
		this.vs = new Value[len];
		Arrays.fill(this.vs, Value.createKnown(BitWidth.ONE, 0));
	}

	public void clear() {
		Arrays.fill(vs, Value.createKnown(BitWidth.ONE, 0));
	}

	@Override
	public ShiftRegisterData clone() {
		ShiftRegisterData ret = (ShiftRegisterData) super.clone();
		ret.vs = this.vs.clone();
		return ret;
	}

	public Value get(int index) {
		if (index >= vs.length)
			index -= vs.length;
		return vs[index];
	}

	public Value getValues() {
		return Value.create(vs);
	}

	public int getLength() {
		return vs.length;
	}

	public void push(Value mode, Value v) {
		if (mode.equals(UniversalRegister.SHIFT_LEFT)) {
			// Left shift
			push(v);
		} else if (mode.equals(UniversalRegister.SHIFT_RIGHT)) {
			// Right shift
			System.arraycopy(vs, 1, vs, 0, vs.length - 1);
			vs[vs.length - 1] = v;
		}
	}

	// Only left shift
	public void push(Value v) {
		System.arraycopy(vs, 0, vs, 1, vs.length - 1);
		vs[0] = v;
	}

	public void parallelLoad(Value[] v) {
		System.arraycopy(v, 0, vs, 0, vs.length);
	}

	public void set(int index, Value val) {
		vs[index] = val;
	}

	public void setDimensions(int newLength) {
		if (vs.length != newLength) {
			Value[] newV = new Value[newLength];
			int copy = Math.min(newLength, vs.length);
			for (int i = 0; i < copy; i++) {
				newV[i] = vs[i];
			}
			Arrays.fill(newV, copy, newLength, Value.createKnown(BitWidth.ONE, 0));
			vs = newV;
		}
	}
}