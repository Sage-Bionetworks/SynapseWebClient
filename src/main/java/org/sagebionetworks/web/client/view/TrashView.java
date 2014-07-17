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
	void displayIndividualRow(TrashedEntity trashedEntity);
	
	public interface Presenter extends SynapsePresenter {
		void deleteAll();
		void selectTrash(TrashedEntity trash);
		void deselectTrash(TrashedEntity trash);
		// TODO: void deleteSelected
		
	}
}
