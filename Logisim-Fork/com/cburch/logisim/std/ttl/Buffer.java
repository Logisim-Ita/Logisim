package com.cburch.logisim.std.ttl;

import java.awt.Graphics;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;

public class Buffer extends AbstractTtlGate {

	public Buffer() {
		super("Buffer", 14, new int[] { 3, 6, 8, 11 }, 2);
	}

	@Override
	public void paintInternal(InstancePainter painter, int x, int y, int height, boolean up) {
		Graphics g = painter.getGraphics();
		int portwidth = 19, portheight = 15;
		int youtput = y + (up ? 20 : 40);
		Drawgates.paintBuffer(g, x + 35, youtput, portwidth, portheight);
		// output line
		Drawgates.paintOutputgate(g, x + 50, y, x + 44, youtput, up);
		// input line
		Drawgates.paintSingleInputgate(g, x + 10, y, x + 35 - portwidth, youtput, up);
		}

	@Override
	public void ttlpropagate(InstanceState state) {
		for (int i = 2; i < 6; i += 3) {
		if(state.getPort(i-1)==Value.TRUE){
			state.setPort(i,state.getPort(i-2),1);
		}
		else {
			state.setPort(i,Value.UNKNOWN,1);
		}
	}
	}

}
