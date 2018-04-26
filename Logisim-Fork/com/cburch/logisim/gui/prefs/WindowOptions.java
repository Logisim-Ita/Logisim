/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.prefs;

import javax.swing.JPanel;

import com.cburch.logisim.data.Direction;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.util.TableLayout;

class WindowOptions extends OptionsPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1043476425449770400L;
	private PrefBoolean[] checks;
	private PrefOptionList toolbarPlacement, Refreshrate;

	public WindowOptions(PreferencesFrame window) {
		super(window);

		checks = new PrefBoolean[] {
				new PrefBoolean(AppPreferences.SHOW_TICK_RATE, Strings.getter("windowTickRate")), };

		toolbarPlacement = new PrefOptionList(AppPreferences.TOOLBAR_PLACEMENT, Strings.getter("windowToolbarLocation"),
				new PrefOption[] { new PrefOption(Direction.NORTH.toString(), Direction.NORTH.getDisplayGetter()),
						new PrefOption(Direction.SOUTH.toString(), Direction.SOUTH.getDisplayGetter()),
						new PrefOption(Direction.EAST.toString(), Direction.EAST.getDisplayGetter()),
						new PrefOption(Direction.WEST.toString(), Direction.WEST.getDisplayGetter()),
						new PrefOption(AppPreferences.TOOLBAR_DOWN_MIDDLE, Strings.getter("windowToolbarDownMiddle")),
						new PrefOption(AppPreferences.TOOLBAR_HIDDEN, Strings.getter("windowToolbarHidden")) });
		Refreshrate = new PrefOptionList(AppPreferences.REFRESH_RATE, Strings.getter("windowRefreshRate"),
				new PrefOption[] { new PrefOption("20", Strings.getter("20Hz")),
						new PrefOption("30", Strings.getter("30Hz")), new PrefOption("60", Strings.getter("60Hz")),
						new PrefOption("120", Strings.getter("120Hz")),
						new PrefOption("144", Strings.getter("144Hz")) });
		JPanel panel = new JPanel(new TableLayout(2));

		panel.add(toolbarPlacement.getJLabel());
		panel.add(toolbarPlacement.getJComboBox());
		panel.add(Refreshrate.getJLabel());
		panel.add(Refreshrate.getJComboBox());
		setLayout(new TableLayout(1));
		for (int i = 0; i < checks.length; i++) {
			add(checks[i]);
		}
		add(panel);
	}

	@Override
	public String getHelpText() {
		return Strings.get("windowHelp");
	}

	@Override
	public String getTitle() {
		return Strings.get("windowTitle");
	}

	@Override
	public void localeChanged() {
		for (int i = 0; i < checks.length; i++) {
			checks[i].localeChanged();
		}
		toolbarPlacement.localeChanged();
		Refreshrate.localeChanged();
	}
}
