package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;

import com.google.gwt.user.client.ui.IsWidget;

public interface EntityMetadataView extends IsWidget, SynapseView {

	public void setPresenter(Presenter p);

	public void setEntityBundle(EntityBundle bundle, boolean canAdmin, boolean canEdit, boolean autoShowFileHistory);

	public void showInfo(String string, String message);
	
	public void setDetailedMetadataVisible(boolean visible);
	public void setEntityNameVisible(boolean visible);

	public void showErrorMessage(String message);
	public void setEntityUpdatedHandler(EntityUpdatedHandler handler);
	
	public interface Presenter {
		void fireEntityUpdatedEvent();
	}



}
