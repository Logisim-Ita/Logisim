/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cburch.draw.toolbar.AbstractToolbarModel;
import com.cburch.draw.toolbar.ToolbarItem;
import com.cburch.draw.toolbar.ToolbarSeparator;
import com.cburch.logisim.Main;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.file.ToolbarData;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectEvent;
import com.cburch.logisim.proj.ProjectListener;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.InputEventUtil;

class LayoutToolbarModel extends AbstractToolbarModel {
	private class MyListener
			implements ProjectListener, AttributeListener, ToolbarData.ToolbarListener, PropertyChangeListener {
		//
		// AttributeListener methods
		//
		@Override
		public void attributeListChanged(AttributeEvent e) {
		}

		@Override
		public void attributeValueChanged(AttributeEvent e) {
			fireToolbarAppearanceChanged();
		}

		//
		// ProjectListener methods
		//
		@Override
		public void projectChanged(ProjectEvent e) {
			int act = e.getAction();
			if (act == ProjectEvent.ACTION_SET_TOOL) {
				fireToolbarAppearanceChanged();
			} else if (act == ProjectEvent.ACTION_SET_FILE) {
				LogisimFile old = (LogisimFile) e.getOldData();
				if (old != null) {
					ToolbarData data = old.getOptions().getToolbarData();
					data.removeToolbarListener(this);
					data.removeToolAttributeListener(this);
				}
				LogisimFile file = (LogisimFile) e.getData();
				if (file != null) {
					ToolbarData data = file.getOptions().getToolbarData();
					data.addToolbarListener(this);
					data.addToolAttributeListener(this);
				}
				buildContents();
			}
		}

		//
		// PropertyChangeListener method
		//
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (AppPreferences.GATE_SHAPE.isSource(event)) {
				fireToolbarAppearanceChanged();
			}
		}

		//
		// ToolbarListener methods
		//
		@Override
		public void toolbarChanged() {
			buildContents();
		}
	}

	private class ToolItem implements ToolbarItem {
		private Tool tool;

		ToolItem(Tool tool) {
			this.tool = tool;
		}

		@Override
		public Dimension getDimension(Object orientation) {
			return new Dimension(24, 24);
		}

		@Override
		public String getToolTip() {
			String ret = tool.getDescription();
			int index = 1;
			for (ToolbarItem item : items) {
				if (item == this)
					break;
				if (item instanceof ToolItem)
					++index;
			}
			if (index <= 10) {
				if (index == 10)
					index = 0;
				int mask = (Main.JAVA_VERSION < 10.0) ? 128 : frame.getToolkit().getMenuShortcutKeyMaskEx();
				ret += " (" + InputEventUtil.toKeyDisplayString(mask) + "-" + index + ")";
			}
			return ret;
		}

		@Override
		public boolean isSelectable() {
			return true;
		}

		@Override
		public void paintIcon(Component destination, Graphics g) {
			// draw halo
			if (tool == haloedTool && AppPreferences.ATTRIBUTE_HALO.getBoolean()) {
				g.setColor(Canvas.HALO_COLOR);
				g.fillRect(1, 1, 22, 22);
			}

			// draw tool icon
			g.setColor(Color.BLACK);
			Graphics g_copy = g.create();
			ComponentDrawContext c = new ComponentDrawContext(destination, null, null, g, g_copy);
			tool.paintIcon(c, 2, 2);
			g_copy.dispose();
		}
	}

	private static ToolbarItem findItem(List<ToolbarItem> items, Tool tool) {
		for (ToolbarItem item : items) {
			if (item instanceof ToolItem) {
				if (tool == ((ToolItem) item).tool) {
					return item;
				}
			}
		}
		return null;
	}

	private Frame frame;
	private Project proj;
	private MyListener myListener;
	private List<ToolbarItem> items;

	private Tool haloedTool;

	public LayoutToolbarModel(Frame frame, Project proj) {
		this.frame = frame;
		this.proj = proj;
		myListener = new MyListener();
		items = Collections.emptyList();
		haloedTool = null;
		buildContents();

		// set up listeners
		ToolbarData data = proj.getOptions().getToolbarData();
		data.addToolbarListener(myListener);
		data.addToolAttributeListener(myListener);
		AppPreferences.GATE_SHAPE.addPropertyChangeListener(myListener);
		proj.addProjectListener(myListener);
	}

	private void buildContents() {
		List<ToolbarItem> oldItems = items;
		List<ToolbarItem> newItems = new ArrayList<ToolbarItem>();
		ToolbarData data = proj.getLogisimFile().getOptions().getToolbarData();
		for (Tool tool : data.getContents()) {
			if (tool == null) {
				newItems.add(new ToolbarSeparator(4));
			} else {
				ToolbarItem i = findItem(oldItems, tool);
				if (i == null) {
					newItems.add(new ToolItem(tool));
				} else {
					newItems.add(i);
				}
			}
		}
		items = Collections.unmodifiableList(newItems);
		fireToolbarContentsChanged();
	}

	@Override
	public List<ToolbarItem> getItems() {
		return items;
	}

	@Override
	public boolean isSelected(ToolbarItem item) {
		if (item instanceof ToolItem) {
			Tool tool = ((ToolItem) item).tool;
			return tool == proj.getTool();
		} else {
			return false;
		}
	}

	@Override
	public void itemSelected(ToolbarItem item) {
		if (item instanceof ToolItem) {
			Tool tool = ((ToolItem) item).tool;
			proj.setTool(tool);
		}
	}

	public void setHaloedTool(Tool t) {
		if (haloedTool != t) {
			haloedTool = t;
			fireToolbarAppearanceChanged();
		}
	}
}
