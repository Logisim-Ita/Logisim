package com.cburch.logisim.std.ttl;

import java.awt.Graphics;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;

public class Buffer extends AbstractTtlGate {

	public Buffer() {
		super("Buffer", 14, new int[] { 3, 6, 8, 11 }, true);
	}


	@Override
	public void paintInternal(InstancePainter painter, int x, int y, int height, boolean up) {
		Graphics g = painter.getGraphics();
		int portwidth = 15, portheight = 10;
		int youtput = y + (up ? 20 : 40);
		Drawgates.paintBuffer(g, x + 50, youtput, portwidth, portheight);
		// output line
		Drawgates.paintOutputgate(g, x + 50, y, x + 45, youtput, up);
		// input line
		Drawgates.paintSingleInputgate(g, x + 30, y, x + 35, youtput, up);
		// enable line
		Drawgates.paintSingleInputgate(g, x + 10, y, x + 42, youtput - 7, up);
		g.drawLine(x + 42, youtput - 7, x + 42, youtput - 6); // drawline(x0,y0,x1,y1) connecting oval to enable line
		g.drawOval(x + 40, youtput - 6, 3, 3);
	}

	@Override
	public void ttlpropagate(InstanceState state) {
		for (int i = 2; i < 6; i += 3) {
			if (state.getPort(i - 2) == Value.FALSE) {
				state.setPort(i, state.getPort(i - 1), 1);
			} else {
				state.setPort(i, Value.UNKNOWN, 1);
			}
		}
	}

}
