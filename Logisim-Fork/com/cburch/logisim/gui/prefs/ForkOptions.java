package com.cburch.logisim.gui.prefs;

import javax.swing.JPanel;

import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.util.LocaleManager;
import com.cburch.logisim.util.TableLayout;

class ForkOptions extends OptionsPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9163994176877542102L;
	private PrefBoolean[] checks;
	private PrefOptionList updates;

	public ForkOptions(PreferencesFrame frame) {
		super(frame);
		checks = new PrefBoolean[] { new PrefBoolean(AppPreferences.ANTI_ALIASING, Strings.getter("AntiAliasing")) };

		updates = new PrefOptionList(AppPreferences.AUTO_UPDATES, Strings.getter("AutoUpdates"),
				new PrefOption[] { new PrefOption(AppPreferences.ALWAYS, Strings.getter("Always")),
						new PrefOption(AppPreferences.ASKME, Strings.getter("AskMe")), new PrefOption(AppPreferences.NO,
								new LocaleManager("resources/logisim", "data").getter("booleanFalseOption")) });

		setLayout(new TableLayout(1));

		JPanel panel = new JPanel(new TableLayout(2));
		for (int i = 0; i < checks.length; i++) {
			add(checks[i]);
		}
		panel.add(updates.getJLabel());
		panel.add(updates.getJComboBox());
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
		updates.localeChanged();
	}

}
