package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;

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
	 * 
	 * @param rootEntities list of entities to make root level nodes for in the tree
	 */
	void setRootEntities(List<EntityHeader> rootEntities);
	
	/**
	 * Remove an entity from the view identified by entityId
	 * @param entityModel
	 */
	void removeEntity(EntityHeader entityHeader);

	/**
	 * Rather than linking to the Entity Page, a clicked entity
	 * in the tree will become selected.
	 */
	void makeSelectable();
	
	/**
	 * Makes a TreeItem and places it in the tree. Gives the created item a "dummy"
	 * child so that the item can be expanded.
	 * @param childToCreate The EntityHeader who's information will be used to create a
	 * 					 	new tree item and place in the tree.
	 * @param parent The EntityHeader that corresponds to the tree item the the created
	 * 				 child will become the child of. Parameter ignored if isRootItem.
	 * @param isRootItem true if the childToCreate is a root item, false otherwise.
	 */
	void createAndPlaceTreeItem(EntityHeader childToCreate, EntityTreeItem parent, boolean isRootItem);
	
	/**
	 * Makes a TreeItem and places it in the root of the tree.
	 * 
	 * @param toCreate The EntityHeader who's information will be used to create
	 * 				   a new tree item and place in the tree.
	 */
	void createAndPlaceRootTreeItem(EntityHeader toCreate);
		
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void getFolderChildren(String entityId, AsyncCallback<List<EntityHeader>> asyncCallback);

		void setSelection(String id);

		int getMaxLimit();

		ImageResource getIconForType(String type);
		
		void expandTreeItemOnOpen(final EntityTreeItem target);
		
		void clearRecordsFetchedChildren();

	}


}
