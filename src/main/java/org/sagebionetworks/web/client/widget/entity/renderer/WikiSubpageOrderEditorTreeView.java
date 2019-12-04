package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTree.SubpageOrderEditorTreeNode;
import com.google.gwt.user.client.ui.IsWidget;

public interface WikiSubpageOrderEditorTreeView extends IsWidget {

	void configure(SubpageOrderEditorTreeNode overallRoot, String ownerObjectId);

	void selectTreeItem(SubpageOrderEditorTreeNode toSelect);

	void moveTreeItem(SubpageOrderEditorTreeNode node, boolean moveUp);

	void setPresenter(Presenter presenter);

	void setSynAlert(IsWidget w);

	void clear();

	public interface Presenter {
		void selectTreeItem(SubpageOrderEditorTreeNode node);

		SubpageOrderEditorTreeNode getSelectedTreeItem();

		SubpageOrderEditorTreeNode getParent(SubpageOrderEditorTreeNode child);
	}
}
