package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;
import org.sagebionetworks.web.client.widget.entity.MoreTreeItem;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface EntityTreeBrowserView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	/**
	 * Rather than linking to the Entity Page, a clicked entity
	 * in the tree will become selected.
	 */
	void makeSelectable();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void setSelection(String id);

		int getMaxLimit();
		
		void expandTreeItemOnOpen(final EntityTreeItem target);
		
		void clearRecordsFetchedChildren();

		void getChildrenFiles(String parentId, EntityTreeItem parent,
				long offset);

		void getFolderChildren(String parentId, EntityTreeItem parent,
				long offset);

		void addMoreButton(MoreTreeItem moreItem, String parentId,
				EntityTreeItem parent, long offset);

	}

	void appendRootEntityTreeItem(EntityTreeItem childToAdd);

	void appendChildEntityTreeItem(EntityTreeItem childToAdd,
			EntityTreeItem parent);
	
	void insertRootEntityTreeItem(EntityTreeItem childToAdd, long offset);

	void insertChildEntityTreeItem(EntityTreeItem childToAdd,
			EntityTreeItem parent, long offset);

	void configureEntityTreeItem(EntityTreeItem childToAdd);

	void placeChildMoreFilesTreeItem(MoreTreeItem childToCreate,
			EntityTreeItem parent, long offset);

	void placeChildMoreFoldersTreeItem(MoreTreeItem childToCreate,
			EntityTreeItem parent, long offset);

	void placeRootMoreFilesTreeItem(MoreTreeItem childToCreate,
			String parentId, long offset);

	void placeRootMoreFoldersTreeItem(MoreTreeItem childToCreate,
			String parentId, long offset);

}
