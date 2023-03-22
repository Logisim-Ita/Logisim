/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitEvent;
import com.cburch.logisim.circuit.CircuitListener;
import com.cburch.logisim.circuit.SubcircuitFactory;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.file.LibraryEvent;
import com.cburch.logisim.file.LibraryEventSource;
import com.cburch.logisim.file.LibraryListener;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectEvent;
import com.cburch.logisim.proj.ProjectListener;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.JTreeDragController;
import com.cburch.logisim.util.JTreeUtil;
import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;

public class ProjectExplorer extends JTree implements LocaleListener {
	private class DeleteAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5854907291919888339L;

		@Override
		public void actionPerformed(ActionEvent event) {
			TreePath path = getSelectionPath();
			if (listener != null && path != null && path.getPathCount() == 2) {
				listener.deleteRequested(new Event(path));
			}
			ProjectExplorer.this.requestFocus();
		}
	}

	private class DragController implements JTreeDragController {
		private boolean canMove(Object draggedNode, Object targetNode) {
			if (listener == null)
				return false;
			if (!(draggedNode instanceof AddTool) || !(targetNode instanceof AddTool))
				return false;
			LogisimFile file = proj.getLogisimFile();
			AddTool dragged = (AddTool) draggedNode;
			AddTool target = (AddTool) targetNode;
			int draggedIndex = file.getTools().indexOf(dragged);
			int targetIndex = file.getTools().indexOf(target);
			if (targetIndex < 0 || draggedIndex < 0)
				return false;
			return true;
		}

		@Override
		public boolean canPerformAction(JTree targetTree, Object draggedNode, int action, Point location) {
			TreePath pathTarget = targetTree.getPathForLocation(location.x, location.y);
			if (pathTarget == null) {
				targetTree.setSelectionPath(null);
				return false;
			}
			targetTree.setSelectionPath(pathTarget);
			if (action == DnDConstants.ACTION_COPY) {
				return false;
			} else if (action == DnDConstants.ACTION_MOVE) {
				Object targetNode = pathTarget.getLastPathComponent();
				return canMove(draggedNode, targetNode);
			} else {
				return false;
			}
		}

		@Override
		public boolean executeDrop(JTree targetTree, Object draggedNode, Object targetNode, int action) {
			if (action == DnDConstants.ACTION_COPY) {
				return false;
			} else if (action == DnDConstants.ACTION_MOVE) {
				if (canMove(draggedNode, targetNode)) {
					if (draggedNode == targetNode)
						return true;
					listener.moveRequested(new Event(null), (AddTool) draggedNode, (AddTool) targetNode);
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	public static class Event {
		private TreePath path;

		private Event(TreePath path) {
			this.path = path;
		}

		public Object getTarget() {
			return path == null ? null : path.getLastPathComponent();
		}

		public TreePath getTreePath() {
			return path;
		}
	}

	public static interface Listener {
		public void deleteRequested(Event event);

		public void doubleClicked(Event event);

		public JPopupMenu menuRequested(Event event);

		public void moveRequested(Event event, AddTool dragged, AddTool target);

		public void selectionChanged(Event event);
	}

	private class MyCellRenderer extends DefaultTreeCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3479316549221679039L;

		@Override
		public java.awt.Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
				boolean expanded, boolean leaf, int row, boolean hasFocus) {
			java.awt.Component ret;
			ret = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

			if (ret instanceof JComponent) {
				JComponent comp = (JComponent) ret;
				comp.setToolTipText(null);
			}
			if (value instanceof Tool) {
				Tool tool = (Tool) value;
				if (ret instanceof JLabel) {
					((JLabel) ret).setText(tool.getDisplayName());
					((JLabel) ret).setIcon(new ToolIcon(tool));
					((JLabel) ret).setToolTipText(tool.getDescription());
				}
			} else if (value instanceof Library) {
				if (ret instanceof JLabel) {
					Library lib = (Library) value;
					String text = lib.getDisplayName();
					if (lib.isDirty())
						text += DIRTY_MARKER;
					((JLabel) ret).setText(text);
				}
			}
			return ret;
		}
	}

	private class MyListener implements MouseListener, TreeSelectionListener, ProjectListener, LibraryListener,
			CircuitListener, PropertyChangeListener {
		private void checkForPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				TreePath path = getPathForLocation(e.getX(), e.getY());
				if (path != null && listener != null) {
					JPopupMenu menu = listener.menuRequested(new Event(path));
					if (menu != null) {
						menu.show(ProjectExplorer.this, e.getX(), e.getY());
					}
				}
			}
		}

		@Override
		public void circuitChanged(CircuitEvent event) {
			int act = event.getAction();
			if (act == CircuitEvent.ACTION_SET_NAME) {
				model.fireStructureChanged();
				// The following almost works - but the labels aren't made
				// bigger, so you get "..." behavior with longer names.
				// model.fireNodesChanged(model.findPaths(event.getCircuit()));
			}
		}

		@Override
		public void libraryChanged(LibraryEvent event) {
			int act = event.getAction();
			if (act == LibraryEvent.ADD_TOOL) {
				if (event.getData() instanceof AddTool) {
					AddTool tool = (AddTool) event.getData();
					if (tool.getFactory() instanceof SubcircuitFactory) {
						SubcircuitFactory fact = (SubcircuitFactory) tool.getFactory();
						fact.getSubcircuit().addCircuitListener(this);
					}
				}
			} else if (act == LibraryEvent.REMOVE_TOOL) {
				if (event.getData() instanceof AddTool) {
					AddTool tool = (AddTool) event.getData();
					if (tool.getFactory() instanceof SubcircuitFactory) {
						SubcircuitFactory fact = (SubcircuitFactory) tool.getFactory();
						fact.getSubcircuit().removeCircuitListener(this);
					}
				}
			} else if (act == LibraryEvent.ADD_LIBRARY) {
				if (event.getData() instanceof LibraryEventSource) {
					((LibraryEventSource) event.getData()).addLibraryListener(subListener);
				}
			} else if (act == LibraryEvent.REMOVE_LIBRARY) {
				if (event.getData() instanceof LibraryEventSource) {
					((LibraryEventSource) event.getData()).removeLibraryListener(subListener);
				}
			}
			Library lib = event.getSource();
			switch (act) {
			case LibraryEvent.DIRTY_STATE:
			case LibraryEvent.SET_NAME:
				model.fireNodesChanged(model.findPaths(lib));
				break;
			case LibraryEvent.MOVE_TOOL:
				model.fireNodesChanged(model.findPathsForTools(lib));
				break;
			case LibraryEvent.SET_MAIN:
				break;
			default:
				model.fireStructureChanged();
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				TreePath path = getPathForLocation(e.getX(), e.getY());
				if (path != null && listener != null) {
					listener.doubleClicked(new Event(path));
				}
			}
		}

		//
		// MouseListener methods
		//
		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			ProjectExplorer.this.requestFocus();
			checkForPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			checkForPopup(e);
		}

		//
		// project/library file/circuit listener methods
		//
		@Override
		public void projectChanged(ProjectEvent event) {
			int act = event.getAction();
			if (act == ProjectEvent.ACTION_SET_TOOL) {
				TreePath path = getSelectionPath();
				if (path != null && path.getLastPathComponent() != event.getTool()) {
					clearSelection();
				}
			} else if (act == ProjectEvent.ACTION_SET_FILE) {
				setFile(event.getLogisimFile());
			} else if (act == ProjectEvent.ACTION_SET_CURRENT) {
				ProjectExplorer.this.repaint();
			}
		}

		//
		// PropertyChangeListener methods
		//
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (AppPreferences.GATE_SHAPE.isSource(event)) {
				repaint();
			}
		}

		private void setFile(LogisimFile lib) {
			model.fireStructureChanged();
			expandRow(0);

			for (Circuit circ : lib.getCircuits()) {
				circ.addCircuitListener(this);
			}

			subListener = new SubListener(); // create new one so that old
												// listeners die away
			for (Library sublib : lib.getLibraries()) {
				if (sublib instanceof LibraryEventSource) {
					((LibraryEventSource) sublib).addLibraryListener(subListener);
				}
			}
		}

		//
		// TreeSelectionListener methods
		//
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			TreePath path = e.getNewLeadSelectionPath();
			if (listener != null) {
				listener.selectionChanged(new Event(path));
			}
		}
	}

	private class MyModel implements TreeModel {
		ArrayList<TreeModelListener> listeners = new ArrayList<TreeModelListener>();

		@Override
		public void addTreeModelListener(TreeModelListener l) {
			listeners.add(l);
		}

		private ArrayList<TreeModelEvent> findPaths(Object value) {
			ArrayList<TreeModelEvent> ret = new ArrayList<TreeModelEvent>();
			ArrayList<Object> stack = new ArrayList<Object>();
			findPathsSub(value, getRoot(), stack, ret);
			return ret;
		}

		private ArrayList<TreeModelEvent> findPathsForTools(Library value) {
			ArrayList<TreeModelEvent> ret = new ArrayList<TreeModelEvent>();
			ArrayList<Object> stack = new ArrayList<Object>();
			findPathsForToolsSub(value, getRoot(), stack, ret);
			return ret;
		}

		private void findPathsForToolsSub(Library value, Object node, ArrayList<Object> stack,
				ArrayList<TreeModelEvent> paths) {
			stack.add(node);
			if (node == value) {
				TreePath path = new TreePath(stack.toArray());
				List<? extends Tool> toolList = value.getTools();
				int[] indices = new int[toolList.size()];
				Object[] tools = new Object[indices.length];
				for (int i = 0; i < indices.length; i++) {
					indices[i] = i;
					tools[i] = toolList.get(i);
				}
				paths.add(new TreeModelEvent(ProjectExplorer.this, path, indices, tools));
			}
			for (Object child : getChildren(node)) {
				findPathsForToolsSub(value, child, stack, paths);
			}
			stack.remove(stack.size() - 1);
		}

		private void findPathsSub(Object value, Object node, ArrayList<Object> stack, ArrayList<TreeModelEvent> paths) {
			stack.add(node);
			if (node == value) {
				TreePath path = new TreePath(stack.toArray());
				paths.add(new TreeModelEvent(ProjectExplorer.this, path));
			}
			for (Object child : getChildren(node)) {
				findPathsSub(value, child, stack, paths);
			}
			stack.remove(stack.size() - 1);
		}

		private void fireNodesChanged(List<TreeModelEvent> events) {
			for (TreeModelEvent e : events) {
				for (TreeModelListener l : listeners) {
					l.treeNodesChanged(e);
				}
			}
		}

		void fireStructureChanged() {
			TreeModelEvent e = new TreeModelEvent(ProjectExplorer.this, new Object[] { model.getRoot() });
			for (TreeModelListener l : listeners) {
				l.treeStructureChanged(e);
			}
			ProjectExplorer.this.repaint();
		}

		@Override
		public Object getChild(Object parent, int index) {
			return getChildren(parent).get(index);
		}

		@Override
		public int getChildCount(Object parent) {
			return getChildren(parent).size();
		}

		private List<?> getChildren(Object parent) {
			if (parent == proj.getLogisimFile()) {
				return ((Library) parent).getElements();
			} else if (parent instanceof Library) {
				List<?> tools =((Library) parent).getTools();
				List<AddTool> list = new ArrayList(Arrays.asList(tools.toArray()));
				for (int i=0; i<list.size();i++) {
					
					try {
						AddTool tool =((AddTool) list.get(i));
						Boolean isVisible=tool.getFactoryDescription().isVisible();
						if(!isVisible) {
							list.remove(i);
							i--;
						}
					} catch (Exception e) {
					}
				}
				//list.remove(0);
				return list;
			} else {
				return Collections.EMPTY_LIST;
			}
		}

		@Override
		public int getIndexOfChild(Object parent, Object query) {
			if (parent == null || query == null)
				return -1;
			int index = -1;
			for (Object child : getChildren(parent)) {
				index++;
				if (child == query)
					return index;
			}
			return -1;
		}

		@Override
		public Object getRoot() {
			return proj.getLogisimFile();
		}

		@Override
		public boolean isLeaf(Object node) {
			return node != proj && !(node instanceof Library);
		}

		@Override
		public void removeTreeModelListener(TreeModelListener l) {
			listeners.remove(l);
		}

		@Override
		public void valueForPathChanged(TreePath path, Object value) {
			TreeModelEvent e = new TreeModelEvent(ProjectExplorer.this, path);
			fireNodesChanged(Collections.singletonList(e));
		}
	}

	private class MySelectionModel extends DefaultTreeSelectionModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5683156318947814968L;

		@Override
		public void addSelectionPath(TreePath path) {
			if (isPathValid(path))
				super.addSelectionPath(path);
		}

		@Override
		public void addSelectionPaths(TreePath[] paths) {
			paths = getValidPaths(paths);
			if (paths != null)
				super.addSelectionPaths(paths);
		}

		private TreePath[] getValidPaths(TreePath[] paths) {
			int count = 0;
			for (int i = 0; i < paths.length; i++) {
				if (isPathValid(paths[i]))
					++count;
			}
			if (count == 0) {
				return null;
			} else if (count == paths.length) {
				return paths;
			} else {
				TreePath[] ret = new TreePath[count];
				int j = 0;
				for (int i = 0; i < paths.length; i++) {
					if (isPathValid(paths[i]))
						ret[j++] = paths[i];
				}
				return ret;
			}
		}

		private boolean isPathValid(TreePath path) {
			if (path == null || path.getPathCount() > 3)
				return false;
			Object last = path.getLastPathComponent();
			return last instanceof Tool;
		}

		@Override
		public void setSelectionPath(TreePath path) {
			if (isPathValid(path))
				super.setSelectionPath(path);
		}

		@Override
		public void setSelectionPaths(TreePath[] paths) {
			paths = getValidPaths(paths);
			if (paths != null)
				super.setSelectionPaths(paths);
		}
	}

	private class SubListener implements LibraryListener {
		@Override
		public void libraryChanged(LibraryEvent event) {
			model.fireStructureChanged();
		}
	}

	private class ToolIcon implements Icon {
		Tool tool;
		Circuit circ = null;

		ToolIcon(Tool tool) {
			this.tool = tool;
			if (tool instanceof AddTool) {
				ComponentFactory fact = ((AddTool) tool).getFactory(false);
				if (fact instanceof SubcircuitFactory) {
					circ = ((SubcircuitFactory) fact).getSubcircuit();
				}
			}
		}

		@Override
		public int getIconHeight() {
			return 20;
		}

		@Override
		public int getIconWidth() {
			return 20;
		}

		@Override
		public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
			// draw halo if appropriate
			if (tool == haloedTool && AppPreferences.ATTRIBUTE_HALO.getBoolean()) {
				g.setColor(Canvas.HALO_COLOR);
				g.fillRoundRect(x, y, getIconWidth(), getIconHeight(), 5, 5);
				g.setColor(Color.BLACK);
			}

			// draw tool icon
			Graphics gIcon = g.create();
			ComponentDrawContext context = new ComponentDrawContext(ProjectExplorer.this, null, null, g, gIcon);
			tool.paintIcon(context, x, y);
			gIcon.dispose();

			// draw magnifying glass if appropriate
			if (circ == proj.getCurrentCircuit()) {
				int tx = x + 13;
				int ty = y + 13;
				int[] xp = { tx - 1, x + 18, x + 20, tx + 1 };
				int[] yp = { ty + 1, y + 20, y + 18, ty - 1 };
				g.setColor(MAGNIFYING_INTERIOR);
				g.fillOval(x + 5, y + 5, 10, 10);
				g.setColor(Color.darkGray);
				g.drawOval(x + 5, y + 5, 10, 10);
				g.fillPolygon(xp, yp, xp.length);
			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2042462291816718805L;

	private static final String DIRTY_MARKER = "*";

	static final Color MAGNIFYING_INTERIOR = new Color(200, 255, 255, 128);

	private Project proj;
	private MyListener myListener = new MyListener();
	private SubListener subListener = new SubListener();
	private MyModel model = new MyModel();
	private MyCellRenderer renderer = new MyCellRenderer();
	private DeleteAction deleteAction = new DeleteAction();
	private Listener listener = null;
	private Tool haloedTool = null;

	public ProjectExplorer(Project proj) {
		super();
		this.proj = proj;

		setModel(model);
		setRootVisible(true);
		addMouseListener(myListener);
		ToolTipManager.sharedInstance().registerComponent(this);

		MySelectionModel selector = new MySelectionModel();
		selector.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setSelectionModel(selector);
		setCellRenderer(renderer);
		JTreeUtil.configureDragAndDrop(this, new DragController());
		addTreeSelectionListener(myListener);

		InputMap imap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), deleteAction);
		ActionMap amap = getActionMap();
		amap.put(deleteAction, deleteAction);

		proj.addProjectListener(myListener);
		proj.addLibraryListener(myListener);
		AppPreferences.GATE_SHAPE.addPropertyChangeListener(myListener);
		myListener.setFile(proj.getLogisimFile());
		LocaleManager.addLocaleListener(this);
	}

	public Tool getSelectedTool() {
		TreePath path = getSelectionPath();
		if (path == null)
			return null;
		Object last = path.getLastPathComponent();
		return last instanceof Tool ? (Tool) last : null;
	}

	@Override
	public void localeChanged() {
		model.fireStructureChanged();
	}

	public void setHaloedTool(Tool t) {
		if (haloedTool == t)
			return;
		haloedTool = t;
		repaint();
	}

	public void setListener(Listener value) {
		listener = value;
	}
}
