package com.cburch.logisim.std.ttl7400;

import java.util.List;

import com.cburch.logisim.tools.FactoryDescription;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;

public class TTL7400 extends Library {
	private static FactoryDescription[] DESCRIPTIONS = {};

	private List<Tool> tools = null;

	public TTL7400() {
	}

	/*@Override
	public String getDisplayName() {
		return Strings.get("ttl7400Library");
	}*/

	@Override
	public String getName() {
		return "TTL7400";
	}

	@Override
	public List<? extends Tool> getTools() {
		if (tools == null) {
			tools = FactoryDescription.getTools(TTL7400.class, DESCRIPTIONS);
		}
		return tools;
	}

}
