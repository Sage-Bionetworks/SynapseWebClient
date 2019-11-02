package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.ProjectListSortColumn;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.presenter.ProjectFilterEnum;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ProfileView extends IsWidget, SynapseView {

	/**
	 * Set this view's presenter
	 * 
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	/**
	 * Renders the view for a given presenter
	 */
	void setProfile(UserProfile profile, boolean isOwner, String orcIdHref);

	void refreshHeader();

	void setProjectContainer(Widget widget);

	void addChallengeWidget(Widget widget);

	void setTeamsContainer(Widget widget);

	void clearChallenges();

	void showChallengesLoading(boolean isVisible);

	void setIsMoreChallengesVisible(boolean isVisible);

	void setTeamNotificationCount(String count);

	void clearTeamNotificationCount();

	void setTabSelected(ProfileArea areaTab);

	void showConfirmDialog(String title, String message, Callback yesCallback);

	void showProjectFiltersUI();

	void hideLoading();

	void setTeamsFilterVisible(boolean isVisible);

	void setTeamsFilterSelected();

	void setMyProjectsFilterSelected();

	void setAllProjectsFilterSelected();

	void setFavoritesFilterSelected();

	void setSharedDirectlyWithMeFilterSelected();

	void setFavoritesHelpPanelVisible(boolean isVisible);

	void setProfileEditButtonVisible(boolean isVisible);

	void addUserProfileModalWidget(IsWidget userProfileModalWidget);

	void setSettingsWidget(Widget w);

	void setDownloadListWidget(IsWidget w);

	void setSortDirection(ProjectListSortColumn column, SortDirection direction);

	public interface Presenter {
		void createProject();

		void createTeam();

		void goTo(Place place);

		void refreshTeams();

		void tabClicked(ProfileArea areaTab);

		void getMoreChallenges();

		void applyFilterClicked(ProjectFilterEnum filterType, Team team);

		void onEditProfile();

		void sort(ProjectListSortColumn column);
	}

	void addMyTeamProjectsFilter();

	void addTeamsFilterTeam(Team team);

	void addOpenInvitesWidget(OpenTeamInvitationsWidget openInvitesWidget);

	void setProfileSynAlertWidget(Widget profileSynAlert);

	void setProjectSynAlertWidget(Widget profileSynAlert);

	void setTeamSynAlertWidget(Widget profileSynAlert);

	void setChallengeSynAlertWidget(Widget challengeSynAlert);

	void showTabs(boolean isOwner);

	void open(String url);

	void setLastActivityOnColumnVisible(boolean visible);

	void setCertifiedValidatedWidget(IsWidget w);

	void showLoginAlert();
}
