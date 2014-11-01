package org.sagebionetworks.web.client.view;

import java.util.List;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.presenter.TeamSearchPresenter;
import org.sagebionetworks.web.client.view.TeamSearchView.Presenter;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface PeopleSearchView extends IsWidget, SynapseView {

	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	void configure(List<UserGroupHeader> users, String searchTerm);
	
	
	public interface Presenter extends SynapsePresenter {
		void search(String searchTerm, Integer offset);
		void goTo(Place place);
		int getOffset();
		List<PaginationEntry> getPaginationEntries(int nPerPage, int nPagesToShow);
	}
}
