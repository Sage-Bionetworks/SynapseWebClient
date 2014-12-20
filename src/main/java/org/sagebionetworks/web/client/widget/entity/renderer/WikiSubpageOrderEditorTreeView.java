package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTree.SubpageOrderEditorTreeNode;

import com.google.gwt.user.client.ui.IsWidget;

public interface WikiSubpageOrderEditorTreeView extends IsWidget, SynapseView {

	void configure(SubpageOrderEditorTreeNode overallRoot);
	void selectTreeItem(SubpageOrderEditorTreeNode toSelect);
	void moveTreeItem(SubpageOrderEditorTreeNode node, boolean moveUp);
	
	void setPresenter(Presenter presenter);
	
	public interface Presenter {
		void selectTreeItem(SubpageOrderEditorTreeNode node);
		SubpageOrderEditorTreeNode getSelectedTreeItem();
		SubpageOrderEditorTreeNode getParent(SubpageOrderEditorTreeNode child);
	}
}
