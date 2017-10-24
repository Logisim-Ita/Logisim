/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.prefs;

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.cburch.logisim.data.Direction;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.util.TableLayout;
import com.sun.java.swing.plaf.motif.MotifLookAndFeel;

class WindowOptions extends OptionsPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1043476425449770400L;
	private PrefBoolean[] checks;
	private PrefOptionList toolbarPlacement;
	private PrefOptionList lookAndFeel;

	public WindowOptions(PreferencesFrame window) {
		super(window);

		lookAndFeel = new PrefOptionList(AppPreferences.LOOK_AND_FEEL, Strings.getter("lookAndFeel"),
				new PrefOption[] {
						new PrefOption(UIManager.getSystemLookAndFeelClassName(), Strings.getter("systemLookAndFeel")),
						new PrefOption(NimbusLookAndFeel.class.getName(), Strings.getter("nimbusLookAndFeel")),
						new PrefOption(MotifLookAndFeel.class.getName(), Strings.getter("motifLookAndFeel")),
						new PrefOption(MetalLookAndFeel.class.getName(), Strings.getter("metalLookAndFeel")), });

		checks = new PrefBoolean[] {
				new PrefBoolean(AppPreferences.SHOW_TICK_RATE, Strings.getter("windowTickRate")), };

		toolbarPlacement = new PrefOptionList(AppPreferences.TOOLBAR_PLACEMENT, Strings.getter("windowToolbarLocation"),
				new PrefOption[] { new PrefOption(Direction.NORTH.toString(), Direction.NORTH.getDisplayGetter()),
						new PrefOption(Direction.SOUTH.toString(), Direction.SOUTH.getDisplayGetter()),
						new PrefOption(Direction.EAST.toString(), Direction.EAST.getDisplayGetter()),
						new PrefOption(Direction.WEST.toString(), Direction.WEST.getDisplayGetter()),
						new PrefOption(AppPreferences.TOOLBAR_DOWN_MIDDLE, Strings.getter("windowToolbarDownMiddle")),
						new PrefOption(AppPreferences.TOOLBAR_HIDDEN, Strings.getter("windowToolbarHidden")) });

		JPanel panel = new JPanel(new TableLayout(2));

		panel.add(toolbarPlacement.getJLabel());
		panel.add(toolbarPlacement.getJComboBox());

		panel.add(lookAndFeel.getJLabel());
		panel.add(lookAndFeel.getJComboBox());

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
		lookAndFeel.localeChanged();
	}
}
