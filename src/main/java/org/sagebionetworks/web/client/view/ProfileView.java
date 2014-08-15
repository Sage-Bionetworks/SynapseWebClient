package org.sagebionetworks.web.client.view;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

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
	void render();
	
	void updateView(UserProfile profile, boolean editable, boolean isOwner, PassingRecord passingRecord, Widget profileFormView);
	void refreshHeader();
	void setProjects(List<EntityHeader> myProjects);
	void setProjectsError(String string);
	void setFavorites(List<EntityHeader> headers);
	void setFavoritesError(String string);
	
	void setChallenges(List<EntityHeader> headers);
	void setChallengesError(String error);
	void setTeams(List<Team> teams);
	void setTeamsError(String error);
	
	public interface Presenter extends SynapsePresenter {

		void updateProfileWithLinkedIn(String requestToken, String verifier);
		
		void redirectToLinkedIn();
		
		void showEditProfile();
		
		void showViewMyProfile();
		
		void createProject(String name);
		
		void createTeam(final String teamName);
		
		void goTo(Place place);
		
		void refreshTeams();
	}
}
