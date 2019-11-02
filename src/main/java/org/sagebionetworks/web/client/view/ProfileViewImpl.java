package org.sagebionetworks.web.client.view;

import static org.sagebionetworks.web.client.DisplayUtils.DO_NOTHING_CLICKHANDLER;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Anchor;
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
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.ProjectListSortColumn;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.TeamSearch;
import org.sagebionetworks.web.client.presenter.ProjectFilterEnum;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.ClickableDiv;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.table.v2.results.SortableTableHeaderImpl;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;
import org.sagebionetworks.web.client.widget.user.BadgeSize;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.LIElement;
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
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProfileViewImpl extends Composite implements ProfileView {

	public interface ProfileViewImplUiBinder extends UiBinder<Widget, ProfileViewImpl> {
	}

	@UiField
	org.gwtbootstrap3.client.ui.Anchor orcIdLink;
	@UiField
	Button editProfileButton;
	@UiField
	Div certifiedValidatedContainer;
	@UiField
	SimplePanel editUserProfilePanel;
	HTML noChallengesHtml = new HTML("<p>This tab shows you challenges you have registered for.</p>" + "<p><a href=\"http://sagebionetworks.org/challenges/\" target=\"_blank\">Challenges</a> are open science, collaborative competitions for evaluating and comparing computational algorithms or solutions to problems.</p>");

	@UiField
	Div userBadgeFooter;
	////// Tabs
	@UiField
	LIElement profileListItem;
	@UiField
	ClickableDiv profileTabItemDiv;
	@UiField
	Anchor profileLink;

	@UiField
	ClickableDiv projectsTabItemDiv;
	@UiField
	Anchor projectsLink;
	@UiField
	LIElement projectsListItem;
	@UiField
	ClickableDiv teamsTabItemDiv;
	@UiField
	Anchor teamsLink;
	@UiField
	LIElement teamsListItem;

	@UiField
	ClickableDiv downloadsTabItemDiv;
	@UiField
	Anchor downloadsLink;
	@UiField
	LIElement downloadsListItem;

	@UiField
	ClickableDiv settingsTabItemDiv;
	@UiField
	Anchor settingsLink;
	@UiField
	LIElement settingsListItem;
	@UiField
	ClickableDiv challengesTabItemDiv;
	@UiField
	Anchor challengesLink;
	@UiField
	LIElement challengesListItem;

	@UiField
	DivElement profileUI;
	@UiField
	DivElement dashboardUI;

	@UiField
	DivElement navtabContainer;

	@UiField
	DivElement profileTabContainer;
	@UiField
	DivElement projectsTabContainer;
	@UiField
	DivElement challengesTabContainer;
	@UiField
	DivElement teamsTabContainer;
	@UiField
	DivElement downloadsTabContainer;
	@UiField
	DivElement settingsTabContainer;

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

	// Headings
	@UiField
	Heading projectsHeading;
	@UiField
	Heading teamsHeading;
	@UiField
	Heading challengesHeading;


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

	// Downloads
	@UiField
	Div downloadsTabContent;

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

	@UiField
	Span teamNotifications;
	@UiField
	Alert loginAlert;
	private Presenter presenter;
	private Header headerWidget;
	@UiField
	Div userBadgeContainer;
	// View profile widgets
	private static Icon defaultProfilePicture = new Icon(IconType.SYN_USER);
	static {
		defaultProfilePicture.addStyleName("font-size-150 lightGreyText");
	}
	UserBadge userBadge;
	AnchorListItem loadingTeamsListItem = new AnchorListItem("Loading...");

	@Inject
	public ProfileViewImpl(ProfileViewImplUiBinder binder, Header headerWidget, UserBadge userBadge) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;

		this.userBadge = userBadge;
		userBadge.setSize(BadgeSize.LARGE);
		userBadgeContainer.add(userBadge);
		headerWidget.configure();
		initTabs();
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
		ClickHandler editProfileClickHandler = event -> presenter.onEditProfile();
		editProfileButton.addClickHandler(editProfileClickHandler);

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
	public void setTeamNotificationCount(String count) {
		teamNotifications.setHTML(DisplayUtils.getBadgeHtml(count));
	}

	@Override
	public void setProfile(UserProfile profile, boolean isOwner, String orcIdHref) {
		userBadgeFooter.setVisible(false);
		// TODO: use large user component to show profile. set ORCiD
		userBadge.configure(profile);

		if (!isOwner) {
			setHighlightBoxUser(DisplayUtils.getDisplayName(profile));
		} else {
			userBadgeFooter.setVisible(true);
		}
		if (orcIdHref != null && orcIdHref.trim().length() > 0) {
			orcIdLink.setVisible(true);
			orcIdLink.setHref(orcIdHref);
			orcIdLink.setText(orcIdHref);
			userBadgeFooter.setVisible(true);
		} else {
			orcIdLink.setVisible(false);
		}
		updateHrefs(profile.getOwnerId());
	}

	@Override
	public void setSettingsWidget(Widget w) {
		settingsTabContent.clear();
		settingsTabContent.add(w);
	}

	@Override
	public void setDownloadListWidget(IsWidget w) {
		downloadsTabContent.clear();
		downloadsTabContent.add(w);
	}

	@Override
	public void showTabs(boolean isOwner) {
		DisplayUtils.hide(settingsListItem);
		DisplayUtils.hide(downloadsListItem);
		openInvitesContainer.setVisible(isOwner);
		if (isOwner) {
			resetHighlightBoxes();
			DisplayUtils.show(settingsListItem);
			DisplayUtils.show(downloadsListItem);
			// show create project and team UI
			DisplayUtils.show(createProjectUI);
			DisplayUtils.show(createTeamUI);
		}
		// Teams
		DisplayUtils.show(navtabContainer);
	}

	private void resetHighlightBoxes() {
		projectsHeading.setText("");
		teamsHeading.setText("");
		challengesHeading.setText("");
		projectsHeading.setVisible(false);
		teamsHeading.setVisible(false);
		challengesHeading.setVisible(false);
	}

	private void setHighlightBoxUser(String displayName) {
		projectsHeading.setText(displayName + "'s Projects");
		teamsHeading.setText(displayName + "'s Teams");
		challengesHeading.setText(displayName + "'s Challenges");
		projectsHeading.setVisible(true);
		teamsHeading.setVisible(true);
		challengesHeading.setVisible(true);
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
		DisplayUtils.hide(navtabContainer);
		// init with loading widget
		projectsTabContent.add(DisplayUtils.getSmallLoadingWidget());

		settingsTabContent.clear();
		downloadsTabContent.clear();
		challengesTabContent.clear();
		hideTabContainers();

		DisplayUtils.hide(createProjectUI);
		DisplayUtils.hide(createTeamUI);
		teamSearchTextBox.setValue("");
		projectSearchTextBox.setValue("");

		// reset tab link text (remove any notifications)
		clearTeamNotificationCount();
		projectFiltersUI.setVisible(false);
		teamFiltersDropDownMenu.clear();
		projectNameColumnHeader.setSortDirection(null);
		lastActivityOnColumnHeader.setSortDirection(null);
		certifiedValidatedContainer.clear();
		loginAlert.setVisible(false);
	}

	@Override
	public void clearTeamNotificationCount() {
		teamNotifications.setHTML("");
	}

	private void hideTabContainers() {
		// hide all tab containers
		DisplayUtils.hide(profileTabContainer);
		DisplayUtils.hide(projectsTabContainer);
		DisplayUtils.hide(challengesTabContainer);
		DisplayUtils.hide(teamsTabContainer);
		DisplayUtils.hide(downloadsTabContainer);
		DisplayUtils.hide(settingsTabContainer);
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
		removeClass("active", profileListItem, projectsListItem, teamsListItem, downloadsListItem, settingsListItem, challengesListItem);

		if (targetTab == Synapse.ProfileArea.PROFILE) {
			setTabSelected(profileListItem, profileTabContainer);
		} else if (targetTab == Synapse.ProfileArea.PROJECTS) {
			setTabSelected(projectsListItem, projectsTabContainer);
		} else if (targetTab == Synapse.ProfileArea.TEAMS) {
			setTabSelected(teamsListItem, teamsTabContainer);
		} else if (targetTab == Synapse.ProfileArea.SETTINGS) {
			setTabSelected(settingsListItem, settingsTabContainer);
		} else if (targetTab == Synapse.ProfileArea.DOWNLOADS) {
			setTabSelected(downloadsListItem, downloadsTabContainer);
		} else if (targetTab == Synapse.ProfileArea.CHALLENGES) {
			setTabSelected(challengesListItem, challengesTabContainer);
		} else {
			showErrorMessage("Unrecognized profile tab: " + targetTab.name());
			return;
		}
	}

	private void setTabSelected(LIElement listItem, DivElement container) {
		// only selects if the list item is visible
		if (UIObject.isVisible(listItem)) {
			listItem.addClassName("active");
			DisplayUtils.show(container);
		} else {
			// if tab is not visible, select profile tab
			profileListItem.addClassName("active");
			DisplayUtils.show(profileTabContainer);
		}
	}

	private void removeClass(String cssClassName, LIElement... elements) {
		for (LIElement liElement : elements) {
			liElement.removeClassName(cssClassName);
		}
	}

	private void initTabs() {
		profileTabItemDiv.addClickHandler(getTabClickHandler(Synapse.ProfileArea.PROFILE));
		projectsTabItemDiv.addClickHandler(getTabClickHandler(Synapse.ProfileArea.PROJECTS));
		teamsTabItemDiv.addClickHandler(getTabClickHandler(Synapse.ProfileArea.TEAMS));
		downloadsTabItemDiv.addClickHandler(getTabClickHandler(Synapse.ProfileArea.DOWNLOADS));
		settingsTabItemDiv.addClickHandler(getTabClickHandler(Synapse.ProfileArea.SETTINGS));
		challengesTabItemDiv.addClickHandler(getTabClickHandler(Synapse.ProfileArea.CHALLENGES));

		profileLink.addClickHandler(DO_NOTHING_CLICKHANDLER);
		projectsLink.addClickHandler(DO_NOTHING_CLICKHANDLER);
		teamsLink.addClickHandler(DO_NOTHING_CLICKHANDLER);
		downloadsLink.addClickHandler(DO_NOTHING_CLICKHANDLER);
		settingsLink.addClickHandler(DO_NOTHING_CLICKHANDLER);
		challengesLink.addClickHandler(DO_NOTHING_CLICKHANDLER);
	}

	private void updateHrefs(String userId) {
		String place = "#!Profile:" + userId;
		profileLink.setHref(place + "/" + Synapse.ProfileArea.PROFILE.toString().toLowerCase());
		projectsLink.setHref(place + "/" + Synapse.ProfileArea.PROJECTS.toString().toLowerCase());
		teamsLink.setHref(place + "/" + Synapse.ProfileArea.TEAMS.toString().toLowerCase());
		downloadsLink.setHref(place + "/" + Synapse.ProfileArea.DOWNLOADS.toString().toLowerCase());
		settingsLink.setHref(place + "/" + Synapse.ProfileArea.SETTINGS.toString().toLowerCase());
		challengesLink.setHref(place + "/" + Synapse.ProfileArea.CHALLENGES.toString().toLowerCase());
	}

	private ClickHandler getTabClickHandler(final Synapse.ProfileArea targetTab) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.tabClicked(targetTab);
			}
		};
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
	public void setProfileEditButtonVisible(boolean isVisible) {
		this.editProfileButton.setVisible(isVisible);
		// this.importLinkedIn.setVisible(isVisible);
	}

	@Override
	public void addUserProfileModalWidget(IsWidget userProfileModalWidget) {
		this.editUserProfilePanel.clear();
		this.editUserProfilePanel.add(userProfileModalWidget);
	}

	@Override
	public void open(String url) {
		Window.open(url, "_self", "");
	}

	@Override
	public void setCertifiedValidatedWidget(IsWidget w) {
		certifiedValidatedContainer.add(w);
	}

	@Override
	public void showLoginAlert() {
		loginAlert.setVisible(true);
	}
}
