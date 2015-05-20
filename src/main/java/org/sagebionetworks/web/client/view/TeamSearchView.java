package org.sagebionetworks.web.client.view;

import java.util.List;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface TeamSearchView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(List<Team> teams, String searchTerm);
	public void showEmptyTeams();
	public interface Presenter extends SynapsePresenter {
		void goTo(Place place);
		int getOffset();
		void search(String searchTerm, Integer offset);
		List<PaginationEntry> getPaginationEntries(int nPerPage, int nPagesToShow);
	}
	public void setSynAlertWidget(Widget asWidget);
}
