package com.cburch.logisim.std.ttl;

import java.util.List;

import com.cburch.logisim.tools.FactoryDescription;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;

public class TTL extends Library {
	private static FactoryDescription[] DESCRIPTIONS = {
			new FactoryDescription("7400", Strings.getter("7400"), "ttl.gif", "Ttl7400"),
			new FactoryDescription("7402", Strings.getter("7402"), "ttl.gif", "Ttl7402"),
			new FactoryDescription("7404", Strings.getter("7404"), "ttl.gif", "Ttl7404"),
			new FactoryDescription("7408", Strings.getter("7408"), "ttl.gif", "Ttl7408"),
			new FactoryDescription("7432", Strings.getter("7432"), "ttl.gif", "Ttl7432"),
			new FactoryDescription("7486", Strings.getter("7486"), "ttl.gif", "Ttl7486"),
			new FactoryDescription("747266", Strings.getter("747266"), "ttl.gif", "Ttl747266"), };

	private List<Tool> tools = null;

	public TTL() {
	}

	@Override
	public String getName() {
		return "TTL";
	}

	@Override
	public List<? extends Tool> getTools() {
		if (tools == null) {
			tools = FactoryDescription.getTools(TTL.class, DESCRIPTIONS);
		}
		return tools;
	}

}
