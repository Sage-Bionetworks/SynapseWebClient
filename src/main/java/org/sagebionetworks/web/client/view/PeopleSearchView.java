package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.view.TeamSearchView.Presenter;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface PeopleSearchView extends IsWidget, SynapseView {

	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	
	public interface Presenter extends SynapsePresenter {
		void search(String searchTerm, Integer offset);
		void goTo(Place place);
	}
}
