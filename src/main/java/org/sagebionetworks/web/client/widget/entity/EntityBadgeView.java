package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.KeyValueDisplay;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface EntityBadgeView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	void setEntity(EntityHeader header);

	void showLoadError(String entityId);
	
	void showLoadingIcon();
	
	void hideLoadingIcon();
	
	void setClickHandler(ClickHandler handler);
		
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		ImageResource getIconForType(String type);
		void getInfo(String nodeId, final AsyncCallback<KeyValueDisplay<String>> callback);
		void entityClicked(EntityHeader entityHeader);
	}

	

}
