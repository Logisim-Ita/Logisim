package com.cburch.logisim.std.ttl;

import java.awt.Graphics;

import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;

public class Ttl7404 extends AbstractTtlGate {

	public Ttl7404() {
		super("7404", (byte) 14, new byte[] { 2, 4, 6, 8, 10, 12 }, true);
	}

	@Override
	public void paintInternal(InstancePainter painter, int x, int y, int height, boolean up) {
		Graphics g = painter.getGraphics();
		int portwidth = 12, portheight = 6;
		int youtput = y + (up ? 20 : 40);
		Drawgates.paintNot(g, x + 26, youtput, portwidth, portheight);
		Drawgates.paintOutputgate(g, x + 30, y, x + 26, youtput, up);
		Drawgates.paintSingleInputgate(g, x + 10, y, x + 26 - portwidth, youtput, up);
	}

	@Override
	public void ttlpropagate(InstanceState state) {
		for (byte i = 1; i < 6; i += 2) {
			state.setPort(i, state.getPort(i - 1).not(), 1);
		}
		for (byte i = 6; i < 12; i += 2) {
			state.setPort(i, state.getPort(i + 1).not(), 1);
		}
	}

}