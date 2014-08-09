package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface EntityTreeBrowserView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * 
	 * @param rootEntities list of entities to make root level nodes for in the tree
	 */
	public void setRootEntities(List<EntityHeader> rootEntities, boolean sort);
	
//	/**
//	 * Remove an entity from the view identified by entityId
//	 * @param entityModel
//	 */
//	public void removeEntity(EntityHeader entityHeader);

	/**
	 * Show links if true
	 * @param makeLinks Make the labels entity links if true 
	 */
	public void setMakeLinks(boolean makeLinks);

	/*
	 * Explicityly sets the height of the widget
	 */
	public void setWidgetHeight(int height);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void getFolderChildren(String entityId, AsyncCallback<List<EntityHeader>> asyncCallback);

		void setSelection(String id);

		int getMaxLimit();

		ImageResource getIconForType(String type);

	}


}
