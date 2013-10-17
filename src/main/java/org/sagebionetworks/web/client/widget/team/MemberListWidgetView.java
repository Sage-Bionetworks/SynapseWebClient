package org.sagebionetworks.web.client.widget.team;

import java.util.List;

import org.sagebionetworks.repo.model.TeamMember;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface MemberListWidgetView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(List<TeamMember> members, String searchTerm, boolean isAdmin);
	
	public interface Presenter extends SynapsePresenter {
		//used for the user profile links
		void goTo(Place place);
		void setIsAdmin(String principalId, boolean isAdmin);
		void removeMember(String principalId);
		
		void jumpToOffset(int offset);
		void search(String searchTerm);
		List<PaginationEntry> getPaginationEntries(int nPerPage, int nPagesToShow);
	}
}
