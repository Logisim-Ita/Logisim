package com.cburch.logisim.std.ttl;

import java.awt.Graphics;

import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.util.GraphicsUtil;

public class Ttl74283 extends AbstractTtlGate {
	public Ttl74283() {
		super("74283", 16);
	}

	@Override
	public void paintInternal(InstancePainter painter, int x, int y, int height, boolean up) {
		Graphics g = painter.getGraphics();
		g.drawRect(x + 10, y + AbstractTtlGate.pinheight + 10, super.pinnumber * 10 - 20,
				height - 2 * AbstractTtlGate.pinheight - 20);
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 7; j++) {
				GraphicsUtil.drawCenteredText(g, super.Ttl74283portnames[j + (i * 7)], x + 10 + j * 20 + i * 20,
						y + height - AbstractTtlGate.pinheight - 7 - i * (height - 2 * AbstractTtlGate.pinheight - 11));
			}
		}
	}

	@Override
	public void ttlpropagate(InstanceState state) {
		byte A1 = state.getPort(4) == Value.TRUE ? (byte) 1 : 0;
		byte A2 = state.getPort(2) == Value.TRUE ? (byte) 2 : 0;
		byte A3 = state.getPort(8) == Value.TRUE ? (byte) 4 : 0;
		byte A4 = state.getPort(10) == Value.TRUE ? (byte) 8 : 0;
		byte B1 = state.getPort(5) == Value.TRUE ? (byte) 1 : 0;
		byte B2 = state.getPort(1) == Value.TRUE ? (byte) 2 : 0;
		byte B3 = state.getPort(7) == Value.TRUE ? (byte) 4 : 0;
		byte B4 = state.getPort(11) == Value.TRUE ? (byte) 8 : 0;
		byte CIN = state.getPort(6) == Value.TRUE ? (byte) 1 : 0;
		byte sum = (byte) (A1 + A2 + A3 + A4 + B1 + B2 + B3 + B4 + CIN);
		if (sum > 15) {
			sum -= 16;
			state.setPort(13, Value.TRUE, 1);
		}
		Value output = Value.createKnown(BitWidth.create(4), sum);
		state.setPort(3, output.get(0), 1);
		state.setPort(0, output.get(1), 1);
		state.setPort(9, output.get(2), 1);
		state.setPort(12, output.get(3), 1);
	}
}
