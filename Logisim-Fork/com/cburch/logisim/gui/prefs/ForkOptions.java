package com.cburch.logisim.gui.prefs;

import javax.swing.JPanel;

import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.util.TableLayout;

class ForkOptions extends OptionsPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9163994176877542102L;
	private PrefBoolean[] checks;

	public ForkOptions(PreferencesFrame frame) {
		super(frame);
		checks = new PrefBoolean[] { new PrefBoolean(AppPreferences.ANTI_ALIASING, Strings.getter("AntiAliasing")),
				new PrefBoolean(AppPreferences.AUTO_UPDATES, Strings.getter("AutoUpdates")), };
		JPanel panel = new JPanel();
		setLayout(new TableLayout(1));
		for (int i = 0; i < checks.length; i++) {
			add(checks[i]);
		}
		add(panel);
	}

	@Override
	public String getHelpText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTitle() {
		return "Fork";
	}

	@Override
	public void localeChanged() {
		for (int i = 0; i < checks.length; i++) {
			checks[i].localeChanged();
		}
	}

}
