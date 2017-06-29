package com.cburch.logisim.std.io;

import java.util.Arrays;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceData;

class DiagramState implements InstanceData {
	private Value LastClock;
	private boolean diagram[][];
	private int Inputs, Length; // current inputs and length (number of states)

	public DiagramState(int inputs, int length) {
		LastClock = Value.UNKNOWN;
		diagram = new boolean[inputs][length];
		clear();
		Inputs = inputs;
		Length = length;
	}

	public void clear() { // set all to false
		for (int i = 0; i < Inputs; i++) {
			for (int j = 0; j < Length; j++) {
				diagram[i][j] = false;
			}
		}
		LastClock = Value.UNKNOWN;
	}

	@Override // kept from ttystate
	public DiagramState clone() {
		try {
			DiagramState ret = (DiagramState) super.clone();
			return ret;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public boolean getState(int i, int j) {
		return diagram[i][j];
	}

	public Value setLastClock(Value newClock) {
		// copy, set and return copy
		Value ret = LastClock;
		LastClock = newClock;
		return ret;
	}

	public void setState(int i, int j, boolean b) {
		diagram[i][j] = b;
	}

	public void updateSize(int inputs, int length) {
		// if it's not the same size
		if (inputs != Inputs || length != Length) {
			int oldinputs = Inputs;
			int oldlength = Length;
			int h;
			// update current inputs and length to not go out of array bouds in
			// clear() function
			Inputs = inputs;
			Length = length;
			// create a copy of old boolean matrix
			boolean olddiagram[][] = Arrays.copyOf(diagram, diagram.length);
			diagram = new boolean[Inputs][Length];
			// set all to false
			clear();
			// set old values in new boolean matrix
			for (int i = 0; i < Inputs && i < oldinputs; i++) {
				h = oldlength - 1;
				for (int j = Length - 1; j >= 0 && h >= 0; j--) {
					diagram[i][j] = olddiagram[i][h];
					h--;
				}
			}
		}
	}
}