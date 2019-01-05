package com.cburch.logisim.gui.prefs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.util.JFileChoosers;
import com.cburch.logisim.util.LocaleManager;

class ForkOptions extends OptionsPanel {
	private class MyListener implements ActionListener, PropertyChangeListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			Object src = event.getSource();
			if (src == selectFolderButton) {
				JFileChooser chooser = JFileChoosers.create();
				chooser.setDialogTitle(Strings.get("selectDialogTitle"));
				chooser.setApproveButtonText(Strings.get("selectDialogButton"));
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int action = chooser.showOpenDialog(getPreferencesFrame());
				if (action == JFileChooser.APPROVE_OPTION) {
					File folder = chooser.getSelectedFile();
					AppPreferences.setLibrariesFolder(folder);
				}
			} else if (src == loadLibrariesFolderAtStartup) {
				boolean enabled = loadLibrariesFolderAtStartup.isSelected();
				libraryFolderField.setEnabled(enabled);
				selectFolderButton.setEnabled(enabled);
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			String prop = event.getPropertyName();
			if (prop.equals(AppPreferences.LIBRARIES_FOLDER_PATH))
				setTemplateField((File) event.getNewValue());
		}

		private void setTemplateField(File f) {
			try {
				libraryFolderField.setText(f == null ? "" : f.getCanonicalPath());
			} catch (IOException e) {
				libraryFolderField.setText(f.getName());
			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 9163994176877542102L;

	private MyListener myListener = new MyListener();

	private PrefBoolean[] checks;
	private PrefOptionList updates, lookAndFeel;
	private JTextField libraryFolderField = new JTextField(40);
	private JButton selectFolderButton = new JButton();
	private PrefBoolean loadLibrariesFolderAtStartup = new PrefBoolean(AppPreferences.LOAD_LIBRARIES_FOLDER_AT_STARTUP,
			Strings.getter("LoadLibrariesAtStartup"));

	public ForkOptions(PreferencesFrame frame) {
		super(frame);
		checks = new PrefBoolean[] { new PrefBoolean(AppPreferences.ANTI_ALIASING, Strings.getter("AntiAliasing")),
				new PrefBoolean(AppPreferences.FILL_COMPONENT_BACKGROUND, Strings.getter("FillComponentBackground")),
				new PrefBoolean(AppPreferences.NEW_TOOLBAR, Strings.getter("UseSimpleToolbar")),
				loadLibrariesFolderAtStartup };

		updates = new PrefOptionList(AppPreferences.AUTO_UPDATES, Strings.getter("AutoUpdates"),
				new PrefOption[] { new PrefOption(AppPreferences.ALWAYS, Strings.getter("Always")),
						new PrefOption(AppPreferences.ASKME, Strings.getter("AskMe")), new PrefOption(AppPreferences.NO,
								new LocaleManager("resources/logisim", "data").getter("booleanFalseOption")) });

		lookAndFeel = new PrefOptionList(AppPreferences.LOOK_AND_FEEL, Strings.getter("lookAndFeel"),
				new PrefOption[] { new PrefOption(AppPreferences.SYSTEM, Strings.getter("systemLookAndFeel")),
						new PrefOption(AppPreferences.NIMBUS, Strings.getter("nimbusLookAndFeel")),
						new PrefOption(AppPreferences.METAL, Strings.getter("metalLookAndFeel")) });

		libraryFolderField.setEnabled(loadLibrariesFolderAtStartup.isSelected());
		selectFolderButton.setEnabled(loadLibrariesFolderAtStartup.isSelected());
		libraryFolderField.setEditable(false);
		selectFolderButton.setText(Strings.get("templateSelectButton"));
		selectFolderButton.addActionListener(myListener);
		loadLibrariesFolderAtStartup.addActionListener(myListener);
		AppPreferences.addPropertyChangeListener(AppPreferences.LIBRARIES_FOLDER_PATH, myListener);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		// auto updates
		setLayout(gridbag);
		int margin = 5;
		Insets sinistra = new Insets(0, margin, 0, 0);
		Insets destra = new Insets(0, 0, 0, margin);
		gbc.insets = sinistra;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(updates.getJLabel(), gbc);
		add(updates.getJLabel());
		gbc.insets = destra;
		gbc.gridx++;
		gridbag.setConstraints(updates.getJComboBox(), gbc);
		add(updates.getJComboBox());

		// theme / layout
		gbc.insets = sinistra;
		gbc.gridx = 0;
		gridbag.setConstraints(lookAndFeel.getJLabel(), gbc);
		add(lookAndFeel.getJLabel());
		gbc.insets = destra;
		gbc.gridx++;
		gridbag.setConstraints(lookAndFeel.getJComboBox(), gbc);
		add(lookAndFeel.getJComboBox());

		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, margin, 0, margin);
		gbc.fill = GridBagConstraints.NONE;
		for (int i = 0; i < checks.length; i++) {
			gridbag.setConstraints(checks[i], gbc);
			add(checks[i]);
		}
		// library folder
		gbc.insets = sinistra;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.weightx = 1.0;
		gridbag.setConstraints(libraryFolderField, gbc);
		add(libraryFolderField);
		gbc.insets = destra;
		gbc.gridx++;
		gbc.weightx = 0.0;
		gridbag.setConstraints(selectFolderButton, gbc);
		add(selectFolderButton);
		myListener.setTemplateField(AppPreferences.getLibrariesFolder());
	}

	@Override
	public String getHelpText() {
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
