package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.model.EntityBundle;

import com.google.gwt.user.client.ui.IsWidget;

public interface SharingAndDataUseConditionWidgetView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(EntityBundle bundle, boolean showChangeLink);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		public void entityUpdated();
	}

}
