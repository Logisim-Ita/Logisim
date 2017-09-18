package com.cburch.logisim.std.ttl;

import com.cburch.logisim.instance.InstanceState;

public class Ttl7404 extends AbstractTtlGate {

	public Ttl7404() {
		super("7404");
	}

	@Override
	public void propagate(InstanceState state) {
		for (int i = 1; i < 12; i += 2) {
			state.setPort(i, state.getPort(i - 1).not(), 1);
		}
	}

}