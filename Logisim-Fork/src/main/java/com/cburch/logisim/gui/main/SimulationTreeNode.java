/* Copyright (c) 2011, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import com.cburch.logisim.comp.ComponentFactory;

public abstract class SimulationTreeNode implements TreeNode {
	@Override
	public abstract Enumeration<? extends TreeNode> children();

	@Override
	public abstract boolean getAllowsChildren();

	@Override
	public abstract TreeNode getChildAt(int childIndex);

	@Override
	public abstract int getChildCount();

	public abstract ComponentFactory getComponentFactory();

	@Override
	public abstract int getIndex(TreeNode node);

	@Override
	public abstract TreeNode getParent();

	public boolean isCurrentView(SimulationTreeModel model) {
		return false;
	}

	@Override
	public abstract boolean isLeaf();
}
