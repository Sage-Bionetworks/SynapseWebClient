package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface EntityTreeBrowserView extends IsWidget, SynapseWidgetView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		PlaceChanger getPlaceChanger();

		void getFolderChildren(String entityId, AsyncCallback<List<EntityHeader>> asyncCallback);

		void setSelection(String id);

		int getMaxLimit();

	}

	public void setRootEntities(List<EntityHeader> rootEntities);
}
