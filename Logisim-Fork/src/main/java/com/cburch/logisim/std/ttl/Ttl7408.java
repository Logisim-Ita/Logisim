package com.cburch.logisim.std.ttl;

import java.awt.Graphics;

import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;

public class Ttl7408 extends AbstractTtlGate {

	public Ttl7408() {
		super("7408", (byte) 14, new byte[] { 3, 6, 8, 11 }, true);
	}

	@Override
	public void paintInternal(InstancePainter painter, int x, int y, int height, boolean up) {
		Graphics g = painter.getGraphics();
		int portwidth = 15, portheight = 15;
		int youtput = y + (up ? 20 : 40);
		Drawgates.paintAnd(g, x + 44, youtput, portwidth, portheight, false);
		// output line
		Drawgates.paintOutputgate(g, x + 50, y, x + 44, youtput, up);
		// input lines
		Drawgates.paintDoubleInputgate(g, x + 30, y, x + 44 - portwidth, youtput, portheight, up, false);
	}

	@Override
	public void ttlpropagate(InstanceState state) {
		for (byte i = 2; i < 6; i += 3) {
			state.setPort(i, state.getPort(i - 1).and(state.getPort(i - 2)), 1);
		}
		for (byte i = 6; i < 12; i += 3) {
			state.setPort(i, state.getPort(i + 1).and(state.getPort(i + 2)), 1);
		}
	}

}