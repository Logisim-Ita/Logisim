/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.gui.hex.HexFile;
import com.cburch.logisim.gui.hex.HexFrame;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.MenuExtender;

/*import Frame.Form;
import Main.main;
*/
class MemMenu implements ActionListener, MenuExtender {
	private Mem factory;
	private Instance instance;
	private Project proj;
	private Frame frame;
	private CircuitState circState;
	private JMenuItem edit;
	private JMenuItem clear;
	private JMenuItem load;
	private JMenuItem save;
	// private JMenuItem assembler;

	MemMenu(Mem factory, Instance instance) {
		this.factory = factory;
		this.instance = instance;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		Object src = evt.getSource();
		if (src == edit)
			doEdit();
		else if (src == clear)
			doClear();
		else if (src == load)
			doLoad(factory instanceof Ram);
		else if (src == save)
			doSave(factory instanceof Ram);
		/*
		 * else if (src == assembler) if(factory instanceof Rom || factory instanceof
		 * Ram) doAss();
		 */
	}

	@Override
	public void configureMenu(JPopupMenu menu, Project proj) {
		this.proj = proj;
		this.frame = proj.getFrame();
		this.circState = proj.getCircuitState();

		Object attrs = instance.getAttributeSet();
		if (attrs instanceof RomAttributes) {
			((RomAttributes) attrs).setProject(proj);
		}

		boolean enabled = circState != null;
		edit = createItem(enabled, Strings.get("ramEditMenuItem"));
		clear = createItem(enabled, Strings.get("ramClearMenuItem"));
		load = createItem(enabled, Strings.get("ramLoadMenuItem"));
		save = createItem(enabled, Strings.get("ramSaveMenuItem"));
		// assembler = createItem(enabled, Strings.get("Assembler"));

		menu.addSeparator();
		menu.add(edit);
		menu.add(clear);
		menu.add(load);
		menu.add(save);
		// menu.add(assembler);
	}

	private JMenuItem createItem(boolean enabled, String label) {
		JMenuItem ret = new JMenuItem(label);
		ret.setEnabled(enabled);
		ret.addActionListener(this);
		return ret;
	}

	private void doClear() {
		MemState s = factory.getState(instance, circState);
		boolean isAllZero = s.getContents().isClear();
		if (isAllZero)
			return;

		int choice = JOptionPane.showConfirmDialog(frame, Strings.get("ramConfirmClearMsg"),
				Strings.get("ramConfirmClearTitle"), JOptionPane.YES_NO_OPTION);
		if (choice == JOptionPane.YES_OPTION) {
			s.getContents().clear();
		}
	}

	private void doEdit() {
		MemState s = factory.getState(instance, circState);
		if (s == null)
			return;
		HexFrame frame = factory.getHexFrame(proj, instance, circState);
		frame.setVisible(true);
		frame.toFront();
	}

	private void doLoad(boolean ram) {
		JFileChooser chooser = proj.createChooser();
		File oldSelected = factory.getCurrentImage(instance);
		if (oldSelected != null)
			chooser.setSelectedFile(oldSelected);
		if (ram)
			chooser.setDialogTitle(Strings.get("ramLoadDialogTitle"));
		else
			chooser.setDialogTitle(Strings.get("romLoadDialogTitle"));
		int choice = chooser.showOpenDialog(frame);
		if (choice == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			try {
				factory.loadImage(circState.getInstanceState(instance), f);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(frame, e.getMessage(), Strings.get("ramLoadErrorTitle"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void doSave(boolean ram) {
		MemState s = factory.getState(instance, circState);

		JFileChooser chooser = proj.createChooser();
		File oldSelected = factory.getCurrentImage(instance);
		if (oldSelected != null)
			chooser.setSelectedFile(oldSelected);
		if (ram)
			chooser.setDialogTitle(Strings.get("ramSaveDialogTitle"));
		else
			chooser.setDialogTitle(Strings.get("romSaveDialogTitle"));
		int choice = chooser.showSaveDialog(frame);
		if (choice == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			try {
				HexFile.save(f, s.getContents());
				factory.setCurrentImage(instance, f);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(frame, e.getMessage(), Strings.get("ramSaveErrorTitle"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	/*
	 * private void doAss() { MemState s = factory.getState(instance, circState); if
	 * (s == null) return; main.start(); KappemblerListener k=new
	 * KappemblerListener(factory,instance,circState);
	 * Form.export.addActionListener(k); }
	 */
}
