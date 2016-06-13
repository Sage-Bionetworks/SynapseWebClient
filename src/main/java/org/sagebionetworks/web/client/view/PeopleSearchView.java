package org.sagebionetworks.web.client.view;

import java.util.List;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface PeopleSearchView extends IsWidget, SynapseView {

	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	void configure(List<UserGroupHeader> users, String searchTerm);
	
	
	public interface Presenter {
		void search(String searchTerm, Integer offset);
		void goTo(Place place);
		int getOffset();
		List<PaginationEntry> getPaginationEntries(int nPerPage, int nPagesToShow);
	}


	public void setSynAlertWidget(Widget asWidget);
	public void setSynAlertWidgetVisible(boolean b);
}
