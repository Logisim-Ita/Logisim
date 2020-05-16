package com.cburch.logisim.std.ttl;

import java.awt.Graphics;

import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;

public class Ttl7402 extends AbstractTtlGate {

	public Ttl7402() {
		super("7402", (byte) 14, new byte[] { 1, 4, 10, 13 }, true);
	}

	@Override
	public void paintInternal(InstancePainter painter, int x, int y, int height, boolean up) {
		Graphics g = painter.getGraphics();
		int portwidth = 18, portheight = 15;
		int youtput = y + (up ? 20 : 40);
		Drawgates.paintOr(g, x + 20, youtput, true, true);
		// output line
		Drawgates.paintOutputgate(g, x + 10, y, x + 16, youtput, up);
		// input lines
		Drawgates.paintDoubleInputgate(g, x + 50, y, x + 16 + portwidth, youtput, portheight, up, true);
	}

	@Override
	public void ttlpropagate(InstanceState state) {
		for (byte i = 0; i < 6; i += 3) {
			state.setPort(i, (state.getPort(i + 1).or(state.getPort(i + 2)).not()), 1);
		}
		for (byte i = 8; i < 12; i += 3) {
			state.setPort(i, (state.getPort(i - 1).or(state.getPort(i - 2)).not()), 1);
		}
	}

}