package com.cburch.logisim.std.ttl;

import java.awt.Graphics;

import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.std.plexers.DisplayDecoder;
import com.cburch.logisim.util.GraphicsUtil;

public class Ttl7447 extends AbstractTtlGate {

	public Ttl7447() {
		super("7447");
	}

	@Override
	public void paintInternal(InstancePainter painter, int x, int y, int height, boolean up) {
		String Ttl7447portnames[] = { "B", "C", "LT", "BI", "RBI", "D", "A", "f", "g", "a", "b", "c", "d", "e" };
		Graphics g = painter.getGraphics();
		g.drawRect(x + 10, y + AbstractTtlGate.pinheight + 10, super.pinnumber * 10 - 20,
				height - 2 * AbstractTtlGate.pinheight - 20);
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 7; j++) {
				GraphicsUtil.drawCenteredText(g, Ttl7447portnames[j + (i * 7)], x + 10 + j * 20 + i * 20,
						y + height - AbstractTtlGate.pinheight - 8 - i * (height - 2 * AbstractTtlGate.pinheight - 12));
			}
		}

	}

	@Override
	public void ttlpropagate(InstanceState state) {
		DisplayDecoder.ComputeDisplayDecoderOutputs(state, DisplayDecoder.getdecval(state, false, 0, 6, 0, 1, 5), 9, 10,
				11, 12, 13, 7, 8, 2, 3, 4);
	}

}
