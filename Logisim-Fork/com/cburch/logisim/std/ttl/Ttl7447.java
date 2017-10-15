package com.cburch.logisim.std.ttl;

import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.std.plexers.DisplayDecoder;

public class Ttl7447 extends AbstractTtlGate {

	protected final static String portnames[] = { "B", "C", "LT", "BI", "RBI", "D", "A", "e", "d", "c", "b", "a", "g",
			"f" };

	public Ttl7447() {
		super("7447", 16, new int[] { 9, 10, 11, 12, 13, 14, 15 }, portnames);
	}

	@Override
	public void paintInternal(InstancePainter painter, int x, int y, int height, boolean up) {
		Drawgates.paintPortNames(painter, x, y, height, portnames);
	}

	@Override
	public void ttlpropagate(InstanceState state) {
		DisplayDecoder.ComputeDisplayDecoderOutputs(state, DisplayDecoder.getdecval(state, false, 0, 6, 0, 1, 5), 11,
				10, 9, 8, 7, 13, 12, 2, 3, 4);
	}

}
