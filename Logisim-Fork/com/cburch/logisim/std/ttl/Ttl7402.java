package com.cburch.logisim.std.ttl;

import com.cburch.logisim.instance.InstanceState;

public class Ttl7402 extends AbstractTtlGate {

	public Ttl7402() {
		super("7402");
	}

	@Override
	public void propagate(InstanceState state) {
		for (int i = 2; i < 12; i += 3) {
			state.setPort(i, (state.getPort(i - 1).or(state.getPort(i - 2)).not()), 1);
		}
	}

}