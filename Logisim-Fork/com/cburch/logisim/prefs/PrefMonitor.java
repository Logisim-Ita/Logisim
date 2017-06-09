/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.prefs;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

public interface PrefMonitor<E> extends PreferenceChangeListener {
	public void addPropertyChangeListener(PropertyChangeListener listener);

	public E get();

	public boolean getBoolean();

	public String getIdentifier();

	public boolean isSource(PropertyChangeEvent event);

	@Override
	public void preferenceChange(PreferenceChangeEvent e);

	public void removePropertyChangeListener(PropertyChangeListener listener);

	public void set(E value);

	public void setBoolean(boolean value);
}
