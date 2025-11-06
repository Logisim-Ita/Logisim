package com.cburch.logisim.gui.plugin;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.cburch.logisim.plugin.PluginFolder;
import com.cburch.logisim.plugin.PluginPreferences;
import com.cburch.logisim.plugin.PluginUtils;
import com.cburch.logisim.util.JFileChoosers;

class PluginOptions extends OptionsPanel {
	

	private class MyListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent event) {
			Object src = event.getSource();
			if (src == pluginFolderButton) {
				JFileChooser chooser = JFileChoosers.create();
				chooser.setDialogTitle(Strings.get("selectDialogTitle"));
				chooser.setApproveButtonText(Strings.get("selectDialogButton"));
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int action = chooser.showOpenDialog(getPreferencesFrame());
				if (action == JFileChooser.APPROVE_OPTION) {
					File folder = chooser.getSelectedFile();
					PluginPreferences.setStringPreference(PluginPreferences.LOGISIM_PATH, folder.toString());
					setPersonalFolderField(folder.toString());
					PluginUtils.requestRestart();
				}
			}
			else if (src == autoUpdateCheckBox) {
				boolean isEnabled = autoUpdateCheckBox.isSelected();
				PluginPreferences.setBooleanPreference(PluginPreferences.AUTO_UPDATE, isEnabled);
			}
			else if (src == autoLoadCheckBox) {
				boolean isEnabled = autoLoadCheckBox.isSelected();
				PluginPreferences.setBooleanPreference(PluginPreferences.AUTO_LOAD, isEnabled);
			}
			else if (src == personalPluginFolder) {
				boolean isEnabled = personalPluginFolder.isSelected();
				pluginFolderField.setEnabled(isEnabled);
				pluginFolderButton.setEnabled(isEnabled);
				PluginPreferences.setBooleanPreference(PluginPreferences.PERSONAL_FOLDER, isEnabled);
				if(!isEnabled) {
					String stdPath=PluginFolder.createStandardPath();
					PluginPreferences.setStringPreference(PluginPreferences.LOGISIM_PATH, stdPath);
					setPersonalFolderField(PluginPreferences.getStringPreference(PluginPreferences.LOGISIM_PATH));
					PluginUtils.requestRestart();
				}
			}
			else if (src==pluginUpdateButton) {
				if (PluginUtils.updateAllPlugin()) {
		    		PluginUtils.requestRestart();
				}
			}
		}

		

		private void setPersonalFolderField(String folder) {
			if(folder!=null) {
				pluginFolderField.setText(folder);
			}
		}
		
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2906509302537899709L;
	

	private MyListener myListener = new MyListener();
	
	private JButton pluginUpdateButton = new JButton();
	private JCheckBox autoUpdateCheckBox = new JCheckBox();
	private JCheckBox autoLoadCheckBox = new JCheckBox();
	private JCheckBox personalPluginFolder = new JCheckBox();
	private JTextField pluginFolderField = new JTextField(40);
	private JButton pluginFolderButton = new JButton();
	
	

	public PluginOptions(PluginFrame frame) {
		super(frame);
		autoUpdateCheckBox.setSelected (PluginPreferences.getBoleanPreference(PluginPreferences.AUTO_UPDATE));
		autoLoadCheckBox.setSelected (PluginPreferences.getBoleanPreference(PluginPreferences.AUTO_LOAD));
		personalPluginFolder.setSelected (PluginPreferences.getBoleanPreference(PluginPreferences.PERSONAL_FOLDER));
		
		pluginUpdateButton.addActionListener(myListener);
		autoUpdateCheckBox.addActionListener(myListener);
		autoLoadCheckBox.addActionListener(myListener);
		personalPluginFolder.addActionListener(myListener);
		pluginFolderButton.addActionListener(myListener);
		
		
		pluginFolderField.setEnabled(personalPluginFolder.isSelected());
		pluginFolderButton.setEnabled(personalPluginFolder.isSelected());
		pluginFolderField.setEditable(false);
		
		pluginUpdateButton.setText(Strings.get("pluginUpdateButton"));
		pluginFolderButton.setText(Strings.get("pluginSelectButton"));
		
		
		
		
		
	    GridBagLayout grid = new GridBagLayout();  
        GridBagConstraints gbc = new GridBagConstraints();  
        setLayout(grid);
        GridBagLayout layout = new GridBagLayout();  
        JPanel panel = new JPanel();
		panel.setLayout(layout);
		gbc.gridx = 0;  
		gbc.gridy = 0;  
		panel.add(new JLabel(Strings.get("pluginUpdateLabel")), gbc);  
		gbc.gridx = 1;  
		gbc.gridy = 0;
		panel.add(pluginUpdateButton, gbc);
		gbc.gridx = 0;  
		gbc.gridy = 1;  
		panel.add(new JLabel(Strings.get("pluginAutoUpdateLabel")), gbc);  
		gbc.gridx = 1;  
		gbc.gridy = 1;  
		panel.add(autoUpdateCheckBox, gbc); 
		gbc.gridx = 0;  
		gbc.gridy = 2;  
		panel.add(new JLabel(Strings.get("pluginAutoLoadLabel")), gbc);  
		gbc.gridx = 1;  
		gbc.gridy = 2;  
		panel.add(autoLoadCheckBox, gbc);
		gbc.gridx = 0;  
		gbc.gridy = 3;  
		panel.add(new JLabel(Strings.get("pluginPersonalLabel")), gbc);  
		gbc.gridx = 1;  
		gbc.gridy = 3;  
		panel.add(personalPluginFolder, gbc);  
		gbc.gridx = 0;  
		gbc.gridy = 4;  
		gbc.fill = GridBagConstraints.HORIZONTAL;  
		panel.add(pluginFolderField, gbc);
		gbc.gridx = 1;  
		gbc.gridy = 4;  
		panel.add(pluginFolderButton, gbc); 
		add(panel);
		myListener.setPersonalFolderField(PluginPreferences.getStringPreference(PluginPreferences.LOGISIM_PATH));
		
		
	}

	@Override
	public String getHelpText() {
		return null;
	}

	@Override
	public String getTitle() {
		return Strings.get("pluginTitle");
	}

}
