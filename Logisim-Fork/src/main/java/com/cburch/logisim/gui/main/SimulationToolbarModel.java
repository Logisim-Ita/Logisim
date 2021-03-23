/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.cburch.draw.toolbar.AbstractToolbarModel;
import com.cburch.draw.toolbar.ToolbarItem;
import com.cburch.draw.toolbar.ToolbarSeparator;
import com.cburch.logisim.circuit.Simulator;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.util.UnmodifiableList;

class SimulationToolbarModel extends AbstractToolbarModel implements ChangeListener, MenuListener.EnabledListener {
	private Project project;
	private LogisimToolbarItem simEnable;
	private LogisimToolbarItem simStep;
	private LogisimToolbarItem tickEnable;
	private LogisimToolbarItem tickStep;
	private LogisimToolbarItem itemLayout;
	private LogisimToolbarItem itemAppearance;
	private List<ToolbarItem> items;

	public SimulationToolbarModel(Project project, MenuListener menu) {
		this.project = project;

		simEnable = new LogisimToolbarItem(menu, "simplay.png", LogisimMenuBar.SIMULATE_ENABLE,
				Strings.getter("simulateEnableStepsTip"));
		simStep = new LogisimToolbarItem(menu, "simstep.png", LogisimMenuBar.SIMULATE_STEP,
				Strings.getter("simulateStepTip"));
		tickEnable = new LogisimToolbarItem(menu, "simtplay.png", LogisimMenuBar.TICK_ENABLE,
				Strings.getter("simulateEnableTicksTip"));
		tickStep = new LogisimToolbarItem(menu, "simtstep.png", LogisimMenuBar.TICK_STEP,
				Strings.getter("simulateTickTip"));
		itemLayout = new LogisimToolbarItem(menu, "projlayo.gif", LogisimMenuBar.EDIT_LAYOUT,
				Strings.getter("projectEditLayoutTip"));
		itemAppearance = new LogisimToolbarItem(menu, "projapp.gif", LogisimMenuBar.EDIT_APPEARANCE,
				Strings.getter("projectEditAppearanceTip"));

		items = UnmodifiableList.create(AppPreferences.NEW_TOOLBAR.getBoolean()
				? new ToolbarItem[] { simEnable, simStep, tickEnable, tickStep, new ToolbarSeparator(4), itemLayout,
						itemAppearance, }
				: new ToolbarItem[] { simEnable, simStep, tickEnable, tickStep, });

		menu.addEnabledListener(this);
		menu.getMenuBar().addEnableListener(this);
		stateChanged(null);
	}

	@Override
	public List<ToolbarItem> getItems() {
		return items;
	}

	@Override
	public boolean isSelected(ToolbarItem item) {
		if (item == itemLayout) {
			return project.getFrame().getEditorView().equals(Frame.EDIT_LAYOUT);
		} else if (item == itemAppearance) {
			return project.getFrame().getEditorView().equals(Frame.EDIT_APPEARANCE);
		} else {
			return false;
		}
	}

	@Override
	public void itemSelected(ToolbarItem item) {
		if (item instanceof LogisimToolbarItem) {
			((LogisimToolbarItem) item).doAction();
		}
	}

	@Override
	public void menuEnableChanged(MenuListener source) {
		fireToolbarAppearanceChanged();
	}

	//
	// ChangeListener methods
	//
	@Override
	public void stateChanged(ChangeEvent e) {
		Simulator sim = project.getSimulator();
		boolean running = sim != null && sim.isRunning();
		boolean ticking = sim != null && sim.isTicking();
		simEnable.setIcon(running ? "simstop.png" : "simplay.png");
		simEnable.setToolTip(
				running ? Strings.getter("simulateDisableStepsTip") : Strings.getter("simulateEnableStepsTip"));
		tickEnable.setIcon(ticking ? "simtstop.png" : "simtplay.png");
		tickEnable.setToolTip(
				ticking ? Strings.getter("simulateDisableTicksTip") : Strings.getter("simulateEnableTicksTip"));
		fireToolbarAppearanceChanged();
	}
}
