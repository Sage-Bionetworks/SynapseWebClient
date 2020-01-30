package org.sagebionetworks.web.client.widget.team;

import java.util.List;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.shared.TeamMemberBundle;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface MemberListWidgetView extends IsWidget {

	/**
	 * Set this view's presenter
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	void setMembersContainer(LoadMoreWidgetContainer loadMoreWidget);

	void addMembers(List<TeamMemberBundle> members, boolean isAdmin);

	void clearMembers();

	void showInfo(String message);

	void showErrorMessage(String message);

	public interface Presenter {
		// used for the user profile links
		void goTo(Place place);

		void removeMember(String principalId);

		void setIsAdmin(String principalId, boolean isAdmin);
	}
}
