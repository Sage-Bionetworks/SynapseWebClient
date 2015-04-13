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

		ImageResource getIconForType(String type);
		
		void expandTreeItemOnOpen(final EntityTreeItem target);
		
		void clearRecordsFetchedChildren();

		void getFolderChildren(EntityTreeItem parent, long offset);

		void getChildrenFiles(EntityTreeItem parent, long offset);

	}

	void placeEntityTreeItem(EntityTreeItem childToCreate,
			EntityTreeItem parent, boolean isRootItem);

	void placeMoreTreeItem(MoreTreeItem moreItem, EntityTreeItem parent);

	void setRootEntitiesFromTreeItem(List<EntityTreeItem> rootEntities);


}
