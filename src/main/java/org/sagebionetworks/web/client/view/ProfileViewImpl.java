package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.Divider;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.ProjectListSortColumn;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.jsinterop.EmptyProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.place.TeamSearch;
import org.sagebionetworks.web.client.presenter.ProjectFilterEnum;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.table.v2.results.SortableTableHeaderImpl;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProfileViewImpl extends Composite implements ProfileView {

	public interface ProfileViewImplUiBinder extends UiBinder<Widget, ProfileViewImpl> {
	}

	@UiField
	SimplePanel editUserProfilePanel;
	HTML noChallengesHtml = new HTML();
	public static final String NO_CHALLENGES_HTML = "<p><a href=\"http://sagebionetworks.org/challenges/\" target=\"_blank\">Challenges</a> are open science, collaborative competitions for evaluating and comparing computational algorithms or solutions to problems.</p>";
	
	public static final String CHALLENGE_TAB_HELP_TEXT ="&#10;Challenges are open science, collaborative competitions for evaluating and comparing computational algorithms or solutions to problems.";
	public static final String CHALLENGES_THAT = "This tab shows challenges that ";
	public static final String IS_REGISTERED_FOR = " is registered for.";
	
	@UiField
	Div profileUI;
	@UiField
	DivElement dashboardUI;

	@UiField
	DivElement profileTabContainer;
	@UiField
	DivElement projectsTabContainer;
	@UiField
	DivElement challengesTabContainer;
	@UiField
	DivElement teamsTabContainer;
	@UiField
	DivElement settingsTabContainer;
	@UiField
	DivElement favoritesTabContainer;

	@UiField
	Heading pageHeaderTitle;
	
	// Project tab
	// filters
	@UiField
	ButtonGroup projectFiltersUI;
	@UiField
	Button allProjectsFilter;
	@UiField
	Button myProjectsFilter;
	@UiField
	Button sharedDirectlyWithMeFilter;

	@UiField
	Button favoritesFilter;
	@UiField
	Button teamFilters;
	@UiField
	DropDownMenu teamFiltersDropDownMenu;

	@UiField
	TextBox projectSearchTextBox;
	@UiField
	Button createProjectButton;
	@UiField
	DivElement createProjectUI;
	@UiField
	FlowPanel projectsTabContent;

	// Project tab
	@UiField
	SortableTableHeaderImpl projectNameColumnHeader;
	@UiField
	SortableTableHeaderImpl lastActivityOnColumnHeader;

	// Teams tab
	@UiField
	TextBox teamSearchTextBox;
	@UiField
	Button createTeamButton;
	@UiField
	DivElement createTeamUI;
	@UiField
	FlowPanel openInvitesContainer;
	@UiField
	FlowPanel teamsTabContent;
	@UiField
	Button teamSearchButton;
	@UiField
	Button projectSearchButton;

	// Challenges
	@UiField
	FlowPanel challengesTabContent;
	@UiField
	Button moreChallengesButton;

	// Favorites
	@UiField
	Div favoritesTabContent;

	// Settings
	@UiField
	FlowPanel settingsTabContent;

	// highlight boxes
	@UiField
	DivElement projectsHighlightBox;
	@UiField
	DivElement challengesHighlightBox;
	@UiField
	DivElement teamsHighlightBox;

	@UiField
	LoadingSpinner challengesLoadingUI;

	@UiField
	FlowPanel favoritesHelpPanel;

	@UiField
	SimplePanel profileSynAlertPanel;
	@UiField
	FlowPanel projectSynAlertPanel;
	@UiField
	SimplePanel teamSynAlertPanel;
	@UiField
	FlowPanel challengeSynAlertPanel;
	
	String profileHeaderText = "";
	Synapse.ProfileArea currentTab;;
	@UiField
	Alert loginAlert;
	private Presenter presenter;
	private Header headerWidget;
	// View profile widgets
	private static Icon defaultProfilePicture = new Icon(IconType.SYN_USER);
	static {
		defaultProfilePicture.addStyleName("font-size-150 lightGreyText");
	}
	AnchorListItem loadingTeamsListItem = new AnchorListItem("Loading...");

	CookieProvider cookies;
	SynapseContextPropsProvider propsProvider;
	@Inject
	public ProfileViewImpl(ProfileViewImplUiBinder binder, Header headerWidget, CookieProvider cookies, SynapseContextPropsProvider propsProvider) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.cookies = cookies;
		this.propsProvider = propsProvider;
		headerWidget.configure();
		projectSearchTextBox.getElement().setAttribute("placeholder", "Project name");
		createProjectButton.addClickHandler(event -> presenter.createProject());
		projectSearchTextBox.addKeyDownHandler(event -> {
			if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
				projectSearchButton.click();
			}
		});
		teamSearchTextBox.getElement().setAttribute("placeholder", "Team name");
		createTeamButton.addClickHandler(event -> presenter.createTeam());

		teamSearchTextBox.addKeyDownHandler(event -> {
			if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
				teamSearchButton.click();
			}
		});

		teamSearchButton.addClickHandler(event -> presenter.goTo(new TeamSearch(teamSearchTextBox.getValue())));
		projectSearchButton.addClickHandler(event -> presenter.goTo(new Search(projectSearchTextBox.getValue())));

		moreChallengesButton.addClickHandler(event -> presenter.getMoreChallenges());
		showChallengesLoading(false);

		favoritesFilter.addClickHandler(event -> presenter.applyFilterClicked(ProjectFilterEnum.FAVORITES, null));
		allProjectsFilter.addClickHandler(event -> presenter.applyFilterClicked(ProjectFilterEnum.ALL, null));
		myProjectsFilter.addClickHandler(event -> presenter.applyFilterClicked(ProjectFilterEnum.CREATED_BY_ME, null));
		sharedDirectlyWithMeFilter.addClickHandler(event -> presenter.applyFilterClicked(ProjectFilterEnum.SHARED_DIRECTLY_WITH_ME, null));

		projectNameColumnHeader.setSortingListener(headerName -> presenter.sort(ProjectListSortColumn.PROJECT_NAME));
		lastActivityOnColumnHeader.setSortingListener(headerName -> presenter.sort(ProjectListSortColumn.LAST_ACTIVITY));
	}

	@Override
	public void setSortDirection(ProjectListSortColumn column, SortDirection direction) {
		org.sagebionetworks.repo.model.table.SortDirection tableSortDirection = SortDirection.ASC.equals(direction) ? org.sagebionetworks.repo.model.table.SortDirection.ASC : org.sagebionetworks.repo.model.table.SortDirection.DESC;
		if (ProjectListSortColumn.PROJECT_NAME.equals(column)) {
			projectNameColumnHeader.setSortDirection(tableSortDirection);
			lastActivityOnColumnHeader.setSortDirection(null);
		} else {
			projectNameColumnHeader.setSortDirection(null);
			lastActivityOnColumnHeader.setSortDirection(tableSortDirection);
		}
	}

	@Override
	public void setTeamsContainer(Widget toAdd) {
		teamsTabContent.clear();
		teamsTabContent.add(toAdd);
	}

	@Override
	public void addOpenInvitesWidget(OpenTeamInvitationsWidget openInvitesWidget) {
		openInvitesContainer.clear();
		openInvitesContainer.add(openInvitesWidget.asWidget());
	}

	@Override
	public void setProfileSynAlertWidget(Widget profileSynAlert) {
		profileSynAlertPanel.setWidget(profileSynAlert);
	}

	@Override
	public void setProjectSynAlertWidget(Widget projectSynAlert) {
		projectSynAlertPanel.clear();
		projectSynAlertPanel.add(projectSynAlert);
	}

	@Override
	public void setChallengeSynAlertWidget(Widget synAlert) {
		challengeSynAlertPanel.clear();
		challengeSynAlertPanel.add(synAlert);
	}

	@Override
	public void setTeamSynAlertWidget(Widget teamSynAlert) {
		teamSynAlertPanel.setWidget(teamSynAlert);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		headerWidget.configure();
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void setLastActivityOnColumnVisible(boolean visible) {
		lastActivityOnColumnHeader.asWidget().setVisible(visible);
	}

	@Override
	public void setFavoritesHelpPanelVisible(boolean isVisible) {
		favoritesHelpPanel.setVisible(isVisible);
	}

	@Override
	public void setProfile(UserProfile profile, boolean isOwner) {
		String displayName = DisplayUtils.getDisplayName(profile);
		profileHeaderText = isOwner ? "Your Profile" : displayName + "'s Profile";
		if (currentTab == ProfileArea.PROFILE)
			pageHeaderTitle.setText(profileHeaderText);
		
		String challengesThatUserIsRegisteredFor = CHALLENGES_THAT + displayName + IS_REGISTERED_FOR;
		noChallengesHtml.setHTML("<p>" + challengesThatUserIsRegisteredFor + "</p>" + NO_CHALLENGES_HTML);
	}

	@Override
	public void setSettingsWidget(Widget w) {
		settingsTabContent.clear();
		settingsTabContent.add(w);
	}

	@Override
	public void addTeamsFilterTeam(final Team team) {
		detachLoadingTeamsListItem();
		AnchorListItem teamFilter = new AnchorListItem(team.getName());
		teamFilter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.applyFilterClicked(ProjectFilterEnum.TEAM, team);
			}
		});
		teamFiltersDropDownMenu.add(teamFilter);
	}

	@Override
	public void addMyTeamProjectsFilter() {
		teamFiltersDropDownMenu.clear();
		AnchorListItem teamFilter = new AnchorListItem("All of my teams");
		teamFilter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.applyFilterClicked(ProjectFilterEnum.ALL_MY_TEAM_PROJECTS, null);
			}
		});
		teamFiltersDropDownMenu.add(teamFilter);
		teamFiltersDropDownMenu.add(new Divider());
		detachLoadingTeamsListItem();
		teamFiltersDropDownMenu.add(loadingTeamsListItem);
	}

	private void detachLoadingTeamsListItem() {
		if (loadingTeamsListItem.getParent() != null) {
			loadingTeamsListItem.removeFromParent();
		}
	}

	@Override
	public void setTeamsFilterVisible(boolean isVisible) {
		teamFilters.setVisible(isVisible);
	}

	@Override
	public void clearChallenges() {
		challengesTabContent.clear();
		challengesTabContent.add(noChallengesHtml);
		noChallengesHtml.setVisible(true);
		setIsMoreChallengesVisible(false);
	}

	@Override
	public void setProjectContainer(Widget toAdd) {
		projectsTabContent.clear();
		projectsTabContent.add(toAdd);
	}

	@Override
	public void addChallengeWidget(Widget toAdd) {
		noChallengesHtml.setVisible(false);
		toAdd.addStyleName("margin-top-10");
		challengesTabContent.add(toAdd);
	}

	@Override
	public void showChallengesLoading(boolean isVisible) {
		challengesLoadingUI.setVisible(isVisible);
	}

	@Override
	public void setIsMoreChallengesVisible(boolean isVisible) {
		moreChallengesButton.setVisible(isVisible);
	}

	@Override
	public void refreshHeader() {
		headerWidget.refresh();
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {}

	@Override
	public void hideLoading() {}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {
		// init with loading widget
		projectsTabContent.add(DisplayUtils.getSmallLoadingWidget());
		challengesTabContent.clear();
		hideTabContainers();

		teamSearchTextBox.setValue("");
		projectSearchTextBox.setValue("");

		projectFiltersUI.setVisible(false);
		teamFiltersDropDownMenu.clear();
		projectNameColumnHeader.setSortDirection(null);
		lastActivityOnColumnHeader.setSortDirection(null);
		loginAlert.setVisible(false);
	}

	private void hideTabContainers() {
		// hide all tab containers
		DisplayUtils.hide(profileTabContainer);
		DisplayUtils.hide(projectsTabContainer);
		DisplayUtils.hide(challengesTabContainer);
		DisplayUtils.hide(teamsTabContainer);
		DisplayUtils.hide(settingsTabContainer);
		DisplayUtils.hide(favoritesTabContainer);
	}

	/**
	 * Used only for setting the view's tab display
	 * 
	 * @param targetTab
	 * @param userSelected
	 */
	@Override
	public void setTabSelected(Synapse.ProfileArea targetTab) {
		// tell presenter what tab we're on only if the user clicked
		if (targetTab == null)
			targetTab = Synapse.ProfileArea.PROFILE; // select tab, set default if needed
		hideTabContainers();
		this.currentTab = targetTab;

		if (targetTab == Synapse.ProfileArea.PROFILE) {
			pageHeaderTitle.setText(profileHeaderText);
			DisplayUtils.show(profileTabContainer);
		} else if (targetTab == Synapse.ProfileArea.PROJECTS) {
			pageHeaderTitle.setText("Your Projects");
			DisplayUtils.show(projectsTabContainer);
		} else if (targetTab == Synapse.ProfileArea.TEAMS) {
			pageHeaderTitle.setText("Your Teams");
			DisplayUtils.show(teamsTabContainer);
		} else if (targetTab == Synapse.ProfileArea.SETTINGS) {
			pageHeaderTitle.setText("Account Settings");
			DisplayUtils.show(settingsTabContainer);
		} else if (targetTab == Synapse.ProfileArea.CHALLENGES) {
			pageHeaderTitle.setText("Your Challenges");
			DisplayUtils.show(challengesTabContainer);
		} else if (targetTab == Synapse.ProfileArea.FAVORITES) {
			pageHeaderTitle.setText("Your Favorites");
			
			EmptyProps props = EmptyProps.create();
			ReactElement component = React.createElementWithSynapseContext(SRC.SynapseComponents.FavoritesPage, props, propsProvider.getJsInteropContextProps());
			ReactDOM.render(component, favoritesTabContainer);
			
			DisplayUtils.show(favoritesTabContainer);
		} else {
			showErrorMessage("Unrecognized profile tab: " + targetTab.name());
			return;
		}
	}

	@Override
	public void showConfirmDialog(String title, String message, Callback yesCallback) {
		DisplayUtils.showConfirmDialog(title, message, yesCallback);
	}

	@Override
	public void showProjectFiltersUI() {
		projectFiltersUI.setVisible(true);
	}

	@Override
	public void setAllProjectsFilterSelected() {
		clearFiltersSelected();
		allProjectsFilter.setActive(true);
	}

	@Override
	public void setFavoritesFilterSelected() {
		clearFiltersSelected();
		favoritesFilter.setActive(true);
	}

	@Override
	public void setMyProjectsFilterSelected() {
		clearFiltersSelected();
		myProjectsFilter.setActive(true);
	}

	@Override
	public void setTeamsFilterSelected() {
		clearFiltersSelected();
		teamFilters.setActive(true);
	}

	@Override
	public void setSharedDirectlyWithMeFilterSelected() {
		clearFiltersSelected();
		sharedDirectlyWithMeFilter.setActive(true);
	}

	private void clearFiltersSelected() {
		allProjectsFilter.setActive(false);
		favoritesFilter.setActive(false);
		myProjectsFilter.setActive(false);
		teamFilters.setActive(false);
		sharedDirectlyWithMeFilter.setActive(false);
	}

	@Override
	public void setUserProfileEditorWidget(IsWidget userProfileEditorWidget) {
		this.editUserProfilePanel.clear();
		this.editUserProfilePanel.add(userProfileEditorWidget);
	}
	
	@Override
	public void open(String url) {
		Window.open(url, "_self", "");
	}

	@Override
	public void showLoginAlert() {
		loginAlert.setVisible(true);
	}
}
