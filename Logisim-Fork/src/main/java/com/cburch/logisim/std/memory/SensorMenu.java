/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.MenuExtender;

/*import Frame.Form;
import Main.main;
*/
class SensorMenu implements ActionListener, MenuExtender {
	private Sensor factory;
	private Instance instance;
	private Project proj;
	private Frame frame;
	private CircuitState circState;
	private JMenuItem load;
	private JMenuItem edit;

	SensorMenu(Sensor factory,Instance instance) {
		this.factory = factory;
		this.instance = instance;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		Object src = evt.getSource();
		if (src == load)
			loadValuesFromFile();
		else if(src==edit) {
			SensorFrame f = new SensorFrame(proj,factory, circState, instance);
			f.setVisible(true);
		}
	}

	@Override
	public void configureMenu(JPopupMenu menu, Project proj) {
		this.proj = proj;
		this.frame = proj.getFrame();
		this.circState = proj.getCircuitState();
		boolean enabled = circState != null;
		load = createItem(enabled, Strings.get("ramLoadMenuItem"));
		edit = createItem(enabled, Strings.get("edit"));

		menu.addSeparator();
		menu.add(load);
		menu.add(edit);
	}

	private JMenuItem createItem(boolean enabled, String label) {
		JMenuItem ret = new JMenuItem(label);
		ret.setEnabled(enabled);
		ret.addActionListener(this);
		return ret;
	}



	private void loadValuesFromFile() {
			Path path=null;
			Loader loader = proj.getLogisimFile().getLoader();
			JFileChooser chooser = proj.createChooser();
			if (factory.selectedFile != null)
				chooser.setSelectedFile(factory.selectedFile);
			chooser.setDialogTitle(Strings.get("sensorLoadDialogTitle"));
			if (loader.getMainFile() != null) {
				chooser.setCurrentDirectory(loader.getMainFile());
			}
			int choice = chooser.showOpenDialog(frame);
			if(choice== JFileChooser.APPROVE_OPTION) {
				path=chooser.getSelectedFile().toPath();
				List<String> lines;
				try {
					lines = Files.readAllLines(path);
				} catch (IOException e) {
					lines= new ArrayList<>();
					lines.add("0");
					JOptionPane.showMessageDialog(proj.getFrame(), Strings.get("SensorLoadMessage")+e.getMessage(), Strings.get("ramLoadErrorTitle"),
							JOptionPane.ERROR_MESSAGE);
				}
				factory.setValuesAttribute(circState.getInstanceState(instance), lines.toString());
			}
	}

}
