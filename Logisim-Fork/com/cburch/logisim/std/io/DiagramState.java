package com.cburch.logisim.std.io;

import java.util.Arrays;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceData;

class DiagramState implements InstanceData {
	// the array cell where to save the actual input value
	private int usedcell = -1;
	private Value LastClock;
	private boolean moveback = false;
	private Boolean diagram[][];
	private int Inputs, Length; // current inputs and length (number of states)

	public DiagramState(int inputs, int length) {
		LastClock = Value.UNKNOWN;
		diagram = new Boolean[inputs][length];
		clear();
		Inputs = inputs;
		Length = length;
	}

	public void clear() { // set all to false
		for (int i = 0; i < Inputs; i++) {
			for (int j = 0; j < Length; j++) {
				diagram[i][j] = null;
			}
		}
		moveback = false;
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

	public boolean getmoveback() {
		return moveback;
	}

	public Boolean getState(int i, int j) {
		return diagram[i][j];
	}

	public int getusedcell() {
		return usedcell;
	}

	public void hastomoveback(boolean b) {
		moveback = b;
	}

	public void moveback() { // move back all old values
		for (int i = 0; i < Inputs; i++) {
			for (int j = 0; j < Length - 1; j++) {
				diagram[i][j] = diagram[i][j + 1];
			}
		}
	}

	public Value setLastClock(Value newClock) {
		// copy, set and return copy
		Value ret = LastClock;
		LastClock = newClock;
		return ret;
	}

	public void setState(int i, int j, Boolean b) {
		diagram[i][j] = b;
	}

	public void setusedcell(int i) {
		usedcell = i;
	}

	public void updateSize(int inputs, int length) {
		// if it's not the same size
		if (inputs != Inputs || length != Length) {
			int oldinputs = Inputs;
			int oldlength = Length;
			// update current inputs and length to not go out of array bouds in
			// clear() function
			Inputs = inputs;
			Length = length;
			// create a copy of old boolean matrix
			Boolean olddiagram[][] = Arrays.copyOf(diagram, diagram.length);
			diagram = new Boolean[Inputs][Length];
			// set all to false
			clear();
			if (usedcell < Length - 1) {
				// set old values in new boolean matrix
				for (int i = 0; i < Inputs && i < oldinputs; i++) {
					for (int j = 0; j < Length && j < oldlength; j++) {
						diagram[i][j] = olddiagram[i][j];
					}
				}
				moveback = false;
			} else {
				int h;
				// set old values in new boolean matrix
				for (int i = 0; i < Inputs && i < oldinputs; i++) {
					h = oldlength - 1;
					for (int j = Length - 1; j >= 0 && h >= 0; j--) {
						diagram[i][j] = olddiagram[i][h - (oldlength - usedcell - 1)];
						h--;
					}
				}
				usedcell = Length - 1;
				moveback = true;
			}
		}
	}
}