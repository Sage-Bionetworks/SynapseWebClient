package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface TrashView extends IsWidget, SynapseView {
	/**
	 * Set this view's Presenter
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	// TODO: Declare methods. Presenter methods like "setUsername".
	void displayTrashedEntity(TrashedEntity trashedEntity);
	void removeDisplayTrashedEntity(TrashedEntity trashedEntity);
	
	
	public interface Presenter extends SynapsePresenter {
		void purgeAll();
		void purgeEntity(TrashedEntity trashedEntity);
		void restoreEntity(TrashedEntity trashedEntity);
		
	}
}
