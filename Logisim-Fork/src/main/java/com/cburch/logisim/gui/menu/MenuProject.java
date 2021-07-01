/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.cburch.logisim.gui.plugin.PluginFrame;
import com.cburch.logisim.plugin.PluginLoader;
import com.cburch.logisim.plugin.PluginUtils;
import com.cburch.logisim.proj.Project;

class MenuProject extends Menu {
	private class MyListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			Object src = event.getSource();
			Project proj = menubar.getProject();
			if (src == plugin) {
				 PluginFrame.showPlugin();
			}else if (src == loadBuiltin) {
				ProjectLibraryActions.doLoadBuiltinLibrary(proj);
			} else if (src == loadLogisim) {
				ProjectLibraryActions.LoadLogisimLibraryFromChooser(proj);
			} else if (src == loadJar) {
				ProjectLibraryActions.LoadJarLibraryFromChooser(proj);
			} else if (src == unload) {
				ProjectLibraryActions.doUnloadLibraries(proj);
			} else if (src == options) {
				JFrame frame = proj.getOptionsFrame(true);
				frame.setVisible(true);
				
            }else if(src instanceof JMenuItem){
	            String text = ((JMenuItem) src).getText();
	            PluginLoader.add(text,proj);
			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6329011866349027157L;

	private LogisimMenuBar menubar;
	private MyListener myListener = new MyListener();

	private MenuItemImpl addCircuit = new MenuItemImpl(this, LogisimMenuBar.ADD_CIRCUIT);
    private JMenuItem plugin = new JMenuItem();
    private JMenu loadInternaPlugin = new JMenu();
    private JMenuItem[] array = PluginUtils.makeArray();
	private JMenu loadLibrary = new JMenu();
	private JMenuItem loadBuiltin = new JMenuItem();
	private JMenuItem loadLogisim = new JMenuItem();
	private JMenuItem loadJar = new JMenuItem();
	private JMenuItem unload = new JMenuItem();
	private MenuItemImpl moveUp = new MenuItemImpl(this, LogisimMenuBar.MOVE_CIRCUIT_UP);
	private MenuItemImpl moveDown = new MenuItemImpl(this, LogisimMenuBar.MOVE_CIRCUIT_DOWN);
	private MenuItemImpl remove = new MenuItemImpl(this, LogisimMenuBar.REMOVE_CIRCUIT);
	private MenuItemImpl setAsMain = new MenuItemImpl(this, LogisimMenuBar.SET_MAIN_CIRCUIT);
	private MenuItemImpl revertAppearance = new MenuItemImpl(this, LogisimMenuBar.REVERT_APPEARANCE);
	private MenuItemImpl layout = new MenuItemImpl(this, LogisimMenuBar.EDIT_LAYOUT);
	private MenuItemImpl appearance = new MenuItemImpl(this, LogisimMenuBar.EDIT_APPEARANCE);
	private MenuItemImpl viewToolbox = new MenuItemImpl(this, LogisimMenuBar.VIEW_TOOLBOX);
	private MenuItemImpl viewSimulation = new MenuItemImpl(this, LogisimMenuBar.VIEW_SIMULATION);
	private MenuItemImpl analyze = new MenuItemImpl(this, LogisimMenuBar.ANALYZE_CIRCUIT);
	private MenuItemImpl stats = new MenuItemImpl(this, LogisimMenuBar.CIRCUIT_STATS);
	private JMenuItem options = new JMenuItem();

	MenuProject(LogisimMenuBar menubar) {
		this.menubar = menubar;

		menubar.registerItem(LogisimMenuBar.ADD_CIRCUIT, addCircuit);
		plugin.addActionListener(myListener);
		loadBuiltin.addActionListener(myListener);
		loadLogisim.addActionListener(myListener);
		loadJar.addActionListener(myListener);
		unload.addActionListener(myListener);
		menubar.registerItem(LogisimMenuBar.MOVE_CIRCUIT_UP, moveUp);
		menubar.registerItem(LogisimMenuBar.MOVE_CIRCUIT_DOWN, moveDown);
		menubar.registerItem(LogisimMenuBar.SET_MAIN_CIRCUIT, setAsMain);
		menubar.registerItem(LogisimMenuBar.REMOVE_CIRCUIT, remove);
		menubar.registerItem(LogisimMenuBar.REVERT_APPEARANCE, revertAppearance);
		menubar.registerItem(LogisimMenuBar.EDIT_LAYOUT, layout);
		menubar.registerItem(LogisimMenuBar.EDIT_APPEARANCE, appearance);
		menubar.registerItem(LogisimMenuBar.VIEW_TOOLBOX, viewToolbox);
		menubar.registerItem(LogisimMenuBar.VIEW_SIMULATION, viewSimulation);
		menubar.registerItem(LogisimMenuBar.ANALYZE_CIRCUIT, analyze);
		menubar.registerItem(LogisimMenuBar.CIRCUIT_STATS, stats);
		options.addActionListener(myListener);
		
        if (array!=null) {
        	for (int i = 0; i < array.length; i++) {
        		loadInternaPlugin.add(array[i]);
        		array[i].addActionListener(myListener);
            }
        }
		loadLibrary.add(loadBuiltin);
		loadLibrary.add(loadLogisim);
		loadLibrary.add(loadJar);

		add(addCircuit);
        add(plugin);
        add(loadInternaPlugin);
		add(loadLibrary);
		add(unload);
		addSeparator();
		add(moveUp);
		add(moveDown);
		add(setAsMain);
		add(remove);
		add(revertAppearance);
		addSeparator();
		add(viewToolbox);
		add(viewSimulation);
		add(layout);
		add(appearance);
		addSeparator();
		add(analyze);
		add(stats);
		addSeparator();
		add(options);

		boolean known = menubar.getProject() != null;
		loadInternaPlugin.setEnabled(known&&array!=null);
		loadLibrary.setEnabled(known);
		loadBuiltin.setEnabled(known);
		loadLogisim.setEnabled(known);
		loadJar.setEnabled(known);
		unload.setEnabled(known);
		options.setEnabled(known);
		computeEnabled();
	}

	@Override
	void computeEnabled() {
		setEnabled(menubar.getProject() != null || addCircuit.hasListeners() || moveUp.hasListeners()
				|| moveDown.hasListeners() || setAsMain.hasListeners() || remove.hasListeners() || layout.hasListeners()
				|| revertAppearance.hasListeners() || appearance.hasListeners() || viewToolbox.hasListeners()
				|| viewSimulation.hasListeners() || analyze.hasListeners() || stats.hasListeners());
		menubar.fireEnableChanged();
	}

	public void localeChanged() {
		setText(Strings.get("projectMenu"));
		addCircuit.setText(Strings.get("projectAddCircuitItem"));
		plugin.setText(Strings.get("projectPluginItem"));
        loadInternaPlugin.setText(Strings.get("projectLoadInternalPlugin"));
		loadLibrary.setText(Strings.get("projectLoadLibraryItem"));
		loadBuiltin.setText(Strings.get("projectLoadBuiltinItem"));
		loadLogisim.setText(Strings.get("projectLoadLogisimItem"));
		loadJar.setText(Strings.get("projectLoadJarItem"));
		unload.setText(Strings.get("projectUnloadLibrariesItem"));
		moveUp.setText(Strings.get("projectMoveCircuitUpItem"));
		moveDown.setText(Strings.get("projectMoveCircuitDownItem"));
		setAsMain.setText(Strings.get("projectSetAsMainItem"));
		remove.setText(Strings.get("projectRemoveCircuitItem"));
		revertAppearance.setText(Strings.get("projectRevertAppearanceItem"));
		layout.setText(Strings.get("projectEditCircuitLayoutItem"));
		appearance.setText(Strings.get("projectEditCircuitAppearanceItem"));
		viewToolbox.setText(Strings.get("projectViewToolboxItem"));
		viewSimulation.setText(Strings.get("projectViewSimulationItem"));
		analyze.setText(Strings.get("projectAnalyzeCircuitItem"));
		stats.setText(Strings.get("projectGetCircuitStatisticsItem"));
		options.setText(Strings.get("projectOptionsItem"));
	}
}
