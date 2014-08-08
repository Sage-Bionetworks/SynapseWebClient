package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.EntityBadgeView.Presenter;
import org.sagebionetworks.web.shared.KeyValueDisplay;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface EntityTreeItemView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	void setEntity(EntityHeader header);

	void showLoadError(String entityId);
	
	void showLoadingChildren();
	
	void showTypeIcon();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		ImageResource getIconForType(String type);
		void getInfo(String nodeId, final AsyncCallback<KeyValueDisplay<String>> callback);
		void entityClicked(EntityHeader entityHeader);
		void showLoadingChildren();
		void showTypeIcon();
		EntityHeader getHeader();
	}
}
