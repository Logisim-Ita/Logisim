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
	private PrefOptionList updates, lookAndFeel;

	public ForkOptions(PreferencesFrame frame) {
		super(frame);
		checks = new PrefBoolean[] { new PrefBoolean(AppPreferences.ANTI_ALIASING, Strings.getter("AntiAliasing")) };

		updates = new PrefOptionList(AppPreferences.AUTO_UPDATES, Strings.getter("AutoUpdates"),
				new PrefOption[] { new PrefOption(AppPreferences.ALWAYS, Strings.getter("Always")),
						new PrefOption(AppPreferences.ASKME, Strings.getter("AskMe")), new PrefOption(AppPreferences.NO,
								new LocaleManager("resources/logisim", "data").getter("booleanFalseOption")) });
		lookAndFeel = new PrefOptionList(AppPreferences.LOOK_AND_FEEL, Strings.getter("lookAndFeel"),
				new PrefOption[] { new PrefOption(AppPreferences.SYSTEM, Strings.getter("systemLookAndFeel")),
						new PrefOption(AppPreferences.NIMBUS, Strings.getter("nimbusLookAndFeel")),
						new PrefOption(AppPreferences.MOTIF, Strings.getter("motifLookAndFeel")),
						new PrefOption(AppPreferences.METAL, Strings.getter("metalLookAndFeel")) });

		setLayout(new TableLayout(1));

		JPanel panel = new JPanel(new TableLayout(2));
		for (int i = 0; i < checks.length; i++) {
			add(checks[i]);
		}
		panel.add(updates.getJLabel());
		panel.add(updates.getJComboBox());

		panel.add(lookAndFeel.getJLabel());
		panel.add(lookAndFeel.getJComboBox());
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
		updates.localeChanged();
		lookAndFeel.localeChanged();
		for (int i = 0; i < checks.length; i++) {
			checks[i].localeChanged();
		}
	}

}
