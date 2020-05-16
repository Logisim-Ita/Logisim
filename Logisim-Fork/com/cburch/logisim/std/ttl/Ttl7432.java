package com.cburch.logisim.std.ttl;

import java.awt.Graphics;

import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;

public class Ttl7432 extends AbstractTtlGate {

	public Ttl7432() {
		super("7432", (byte) 14, new byte[] { 3, 6, 8, 11 }, true);
	}

	@Override
	public void paintInternal(InstancePainter painter, int x, int y, int height, boolean up) {
		Graphics g = painter.getGraphics();
		int portwidth = 14, portheight = 15;
		int youtput = y + (up ? 20 : 40);
		Drawgates.paintOr(g, x + 40, youtput, false, false);
		// output line
		Drawgates.paintOutputgate(g, x + 50, y, x + 40, youtput, up);
		// input lines
		Drawgates.paintDoubleInputgate(g, x + 30, y, x + 40 - portwidth, youtput, portheight, up, false);
	}

	@Override
	public void ttlpropagate(InstanceState state) {
		for (byte i = 2; i < 6; i += 3) {
			state.setPort(i, state.getPort(i - 1).or(state.getPort(i - 2)), 1);
		}
		for (byte i = 6; i < 12; i += 3) {
			state.setPort(i, state.getPort(i + 1).or(state.getPort(i + 2)), 1);
		}
	}

}