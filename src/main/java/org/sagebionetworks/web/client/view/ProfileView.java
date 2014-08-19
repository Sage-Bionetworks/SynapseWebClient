package org.sagebionetworks.web.client.view;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.shared.MembershipInvitationBundle;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ProfileView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	/**
	 * Renders the view for a given presenter
	 */
	void updateView(UserProfile profile, boolean isOwner, PassingRecord passingRecord, Widget profileFormView, ProfileArea initialTab);
	void refreshHeader();
	void setProjects(List<EntityHeader> myProjects);
	void setProjectsError(String string);
	void setFavorites(List<EntityHeader> headers);
	void setFavoritesError(String string);
	void setChallenges(List<EntityHeader> headers);
	void setChallengesError(String error);
	void setTeams(List<Team> teams);
	void setTeamsError(String error);
	void setTeamNotificationCount(String count);
	
	public interface Presenter extends SynapsePresenter {
		void updateProfileWithLinkedIn(String requestToken, String verifier);
		void redirectToLinkedIn();
		void createProject(String name);
		void createTeam(final String teamName);
		void goTo(Place place);
		void refreshTeams();
		void updateArea(ProfileArea area);
		void updateTeamInvites(List<MembershipInvitationBundle> invites);
		void addMembershipRequests(int count);
	}
}
