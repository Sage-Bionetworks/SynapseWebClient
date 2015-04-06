package org.sagebionetworks.web.client.view;

import java.util.List;

import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.ProjectListSortColumn;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.presenter.ProjectFilterEnum;
import org.sagebionetworks.web.client.presenter.SortOptionEnum;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.profile.UserProfileModalWidget;
import org.sagebionetworks.web.shared.ChallengeBundle;
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
	void updateView(UserProfile profile, boolean isOwner, PassingRecord passingRecord);
	void refreshHeader();
	void addProjects(List<ProjectHeader> myProjects);
	void setProjectsError(String string);
	void addChallenges(List<ChallengeBundle> challenges);
	void setChallengesError(String error);
	void clearChallenges();
	void showChallengesLoading(boolean isVisible);
	void setIsMoreChallengesVisible(boolean isVisible);
	void showTeamsLoading();
	void setTeams(List<Team> teams, boolean isOwner);
	void setTeamsError(String error);
	void setTeamNotificationCount(String count);
	void clearProjects();
	void setSortText(String text);
	void setIsMoreProjectsVisible(boolean isVisible);
	void clearTeamNotificationCount();
	void refreshTeamInvites();
	void setTabSelected(ProfileArea areaTab);
	void showConfirmDialog(String title, String message, Callback yesCallback);
	void showProjectsLoading(boolean isLoading);
	void showProjectFiltersUI();
	void hideLoading();
	void setTeamsFilterTeams(List<Team> teams);
	void setTeamsFilterVisible(boolean isVisible);
	void setTeamsFilterSelected();
	void setMyProjectsFilterSelected();
	void setAllProjectsFilterSelected();
	void setFavoritesFilterSelected();
	void setSharedDirectlyWithMeFilterSelected();
	void setFavoritesHelpPanelVisible(boolean isVisible);
	void showProfile();
	void hideProfile();
	void setShowProfileButtonVisible(boolean isVisible);
	void setHideProfileButtonVisible(boolean isVisible);
	void setProfileEditButtonVisible(boolean isVisible);
	void addUserProfileModalWidget(IsWidget userProfileModalWidget);
	void addSortOption(SortOptionEnum sort);
	void clearSortOptions();
	void setWelcomeToDashboardVisible(boolean isVisible);
	
	public interface Presenter extends SynapsePresenter {
		void showProfileButtonClicked();
		void hideProfileButtonClicked();
		void updateProfileWithLinkedIn(String requestToken, String verifier);
		void createProject(String name);
		void createTeam(final String teamName);
		void goTo(Place place);
		void refreshTeams();
		void updateArea(ProfileArea area);
		void updateTeamInvites(List<MembershipInvitationBundle> invites);
		void addMembershipRequests(int count);
		void tabClicked(ProfileArea areaTab);
		void certificationBadgeClicked();
		void getMoreProjects();
		void getMoreChallenges();
		void applyFilterClicked(ProjectFilterEnum filterType, Team team);
		void onEditProfile();
		void onImportLinkedIn();
		void welcomeToDashboardDismissed();
		void resort(String text);
	}


}
