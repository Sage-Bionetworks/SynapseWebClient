package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface EntityTreeBrowserView extends IsWidget, SynapseWidgetView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * 
	 * @param rootEntities list of entities to make root level nodes for in the tree
	 */
	public void setRootEntities(List<EntityHeader> rootEntities);
	
	/**
	 * Remove an entity from the view identified by entityId
	 * @param entityModel
	 */
	public void removeEntity(EntityTreeModel entityModel);

	/**
	 * Show links if true
	 * @param makeLinks Make the labels entity links if true 
	 */
	public void setMakeLinks(boolean makeLinks);

	/**
	 * Show the right click menu
	 * @param showContextMenu
	 */
	public void setShowContextMenu(boolean showContextMenu);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void getFolderChildren(String entityId, AsyncCallback<List<EntityHeader>> asyncCallback);

		void setSelection(String id);

		int getMaxLimit();

		ImageResource getIconForType(String type);

		void deleteEntity(EntityTreeModel model);

		void onEdit(String entityId);

	}

}
