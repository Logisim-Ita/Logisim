/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.log;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.Simulator;
import com.cburch.logisim.circuit.SimulatorEvent;
import com.cburch.logisim.circuit.SimulatorListener;
import com.cburch.logisim.file.LibraryEvent;
import com.cburch.logisim.file.LibraryListener;
import com.cburch.logisim.gui.generic.LFrame;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectEvent;
import com.cburch.logisim.proj.ProjectListener;
import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;
import com.cburch.logisim.util.StringUtil;
import com.cburch.logisim.util.WindowMenuItemManager;

public class LogFrame extends LFrame {
	private class MyListener
			implements ActionListener, ProjectListener, LibraryListener, SimulatorListener, LocaleListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			Object src = event.getSource();
			if (src == clearLog) {
				getModel().clearAllLogs();
				getModel().fireSelectionChanged(new ModelEvent());
			} else if (src == alwaysOnTop) {
				setAlwaysOnTopWindow(alwaysOnTop.isSelected());
			}
		}

		@Override
		public void libraryChanged(LibraryEvent event) {
			int action = event.getAction();
			if (action == LibraryEvent.SET_NAME) {
				setTitle(computeTitle(curModel, project));
			}
		}

		@Override
		public void localeChanged() {
			setTitle(computeTitle(curModel, project));
			for (int i = 0; i < panels.length; i++) {
				tabbedPane.setTitleAt(i, panels[i].getTitle());
				tabbedPane.setToolTipTextAt(i, panels[i].getToolTipText());
				panels[i].localeChanged();
			}
			clearLog.setText(Strings.get("clearLogButton"));
			alwaysOnTop.setText(Strings.get("alwaysOnTopCheckBox"));
			windowManager.localeChanged();
		}

		@Override
		public void projectChanged(ProjectEvent event) {
			int action = event.getAction();
			if (action == ProjectEvent.ACTION_SET_STATE) {
				setSimulator(event.getProject().getSimulator(), event.getProject().getCircuitState());
			} else if (action == ProjectEvent.ACTION_SET_FILE) {
				setTitle(computeTitle(curModel, project));
			}
		}

		@Override
		public void propagationCompleted(SimulatorEvent e) {
			curModel.propagationCompleted();
		}

		@Override
		public void simulatorStateChanged(SimulatorEvent e) {
		}

		@Override
		public void tickCompleted(SimulatorEvent e) {
		}
	}

	// TODO should automatically repaint icons when component attr change
	// TODO ? moving a component using Select tool removes it from selection
	private class WindowMenuManager extends WindowMenuItemManager
			implements LocaleListener, ProjectListener, LibraryListener {
		WindowMenuManager() {
			super(Strings.get("logFrameMenuItem"), false);
			project.addProjectListener(this);
			project.addLibraryListener(this);
		}

		@Override
		public JFrame getJFrame(boolean create) {
			return LogFrame.this;
		}

		@Override
		public void libraryChanged(LibraryEvent event) {
			if (event.getAction() == LibraryEvent.SET_NAME) {
				localeChanged();
			}
		}

		@Override
		public void localeChanged() {
			String title = project.getLogisimFile().getDisplayName();
			setText(StringUtil.format(Strings.get("logFrameMenuItem"), title));
		}

		@Override
		public void projectChanged(ProjectEvent event) {
			if (event.getAction() == ProjectEvent.ACTION_SET_FILE) {
				localeChanged();
			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -1483284271691290378L;

	private static String computeTitle(Model data, Project proj) {
		String name = data == null ? "???" : data.getCircuitState().getCircuit().getName();
		return StringUtil.format(Strings.get("logFrameTitle"), name, proj.getLogisimFile().getDisplayName());
	}

	private Project project;
	private Simulator curSimulator = null;
	private Model curModel;
	private Map<CircuitState, Model> modelMap = new HashMap<CircuitState, Model>();
	private MyListener myListener = new MyListener();

	private WindowMenuManager windowManager;
	private LogPanel[] panels;
	private JTabbedPane tabbedPane;

	private JButton clearLog = new JButton();
	private JCheckBox alwaysOnTop = new JCheckBox();

	public LogFrame(Project project) {
		this.project = project;
		this.windowManager = new WindowMenuManager();
		project.addProjectListener(myListener);
		project.addLibraryListener(myListener);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setJMenuBar(new LogisimMenuBar(this, project));
		setSimulator(project.getSimulator(), project.getCircuitState());

		panels = new LogPanel[] { new SelectionPanel(this), new ScrollPanel(this), new FilePanel(this), };
		tabbedPane = new JTabbedPane();
		for (int index = 0; index < panels.length; index++) {
			LogPanel panel = panels[index];
			tabbedPane.addTab(panel.getTitle(), null, panel, panel.getToolTipText());
		}

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(clearLog);
		clearLog.addActionListener(myListener);
		
		buttonPanel.add(alwaysOnTop);
		alwaysOnTop.addActionListener(myListener);

		Container contents = getContentPane();
		tabbedPane.setPreferredSize(new Dimension(450, 300));
		contents.add(tabbedPane, BorderLayout.CENTER);
		contents.add(buttonPanel, BorderLayout.SOUTH);

		LocaleManager.addLocaleListener(myListener);
		myListener.localeChanged();
		pack();
		setLocationRelativeTo(null);
	}

	Model getModel() {
		return curModel;
	}

	LogPanel[] getPrefPanels() {
		return panels;
	}

	public Project getProject() {
		return project;
	}

	private void setSimulator(Simulator value, CircuitState state) {
		if ((value == null) == (curModel == null)) {
			if (value == null || value.getCircuitState() == curModel.getCircuitState())
				return;
		}

		LogisimMenuBar menubar = (LogisimMenuBar) getJMenuBar();
		menubar.setCircuitState(value, state);

		if (curSimulator != null)
			curSimulator.removeSimulatorListener(myListener);
		if (curModel != null)
			curModel.setSelected(this, false);

		Model oldModel = curModel;
		Model data = null;
		if (value != null) {
			data = modelMap.get(value.getCircuitState());
			if (data == null) {
				data = new Model(value.getCircuitState());
				modelMap.put(data.getCircuitState(), data);
			}
		}
		curSimulator = value;
		curModel = data;

		if (curSimulator != null)
			curSimulator.addSimulatorListener(myListener);
		if (curModel != null)
			curModel.setSelected(this, true);
		setTitle(computeTitle(curModel, project));
		if (panels != null) {
			for (int i = 0; i < panels.length; i++) {
				panels[i].modelChanged(oldModel, curModel);
			}
		}
	}

	@Override
	public void setVisible(boolean value) {
		if (value) {
			windowManager.frameOpened(this);
		}
		super.setVisible(value);
	}
	
	public void setAlwaysOnTopWindow (boolean value) {
		super.setAlwaysOnTop(value);
	}
}
