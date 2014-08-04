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
	public void setPresenter(Presenter presenter);
	
	/**
	 * Renders the view for a given presenter
	 */
	public void render();
	
	public void updateView(UserProfile profile, List<Team> teams, boolean editable, boolean isOwner, PassingRecord passingRecord, Widget profileFormView);
	public void refreshHeader();
	public void setMyProjects(List<EntityHeader> myProjects);
	public void setMyProjectsError(String string);
	
	public interface Presenter extends SynapsePresenter {

		void updateProfileWithLinkedIn(String requestToken, String verifier);
		
		void redirectToLinkedIn();
		
		void showEditProfile();
		
		void showViewMyProfile();
		
		void createProject(String name);
		
		void createTeam(final String teamName);
		
		void goTo(Place place);
	}
}
