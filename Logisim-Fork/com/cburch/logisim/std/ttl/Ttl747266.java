package com.cburch.logisim.std.ttl;

import com.cburch.logisim.instance.InstanceState;

public class Ttl747266 extends AbstractTtlGate {

	public Ttl747266() {
		super("747266");
	}

	@Override
	public void propagate(InstanceState state) {
		for (int i = 2; i < 12; i += 3) {
			state.setPort(i, (state.getPort(i - 1).xor(state.getPort(i - 2)).not()), 1);
		}
	}

}