package com.cburch.logisim.std.ttl;

import com.cburch.logisim.instance.InstanceState;

public class Ttl7408 extends AbstractTtlGate {

	public Ttl7408() {
		super("7408");
	}

	@Override
	public void propagate(InstanceState state) {
		for (int i = 2; i < 12; i += 3) {
			state.setPort(i, state.getPort(i - 1).and(state.getPort(i - 2)), 1);
		}
	}

}