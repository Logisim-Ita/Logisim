/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.instance;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.proj.Project;

public interface InstanceState {
	public void fireInvalidated();

	public AttributeSet getAttributeSet();

	public <E> E getAttributeValue(Attribute<E> attr);

	public InstanceData getData();

	public InstanceFactory getFactory();

	public Instance getInstance();

	public Value getPort(int portIndex);

	public Project getProject();

	public long getTickCount();

	public boolean isCircuitRoot();

	public boolean isPortConnected(int portIndex);

	public void setData(InstanceData value);

	public void setPort(int portIndex, Value value, int delay);
}
