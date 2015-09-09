package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.shared.event.AlertClosedEvent;
import org.gwtbootstrap3.client.shared.event.AlertClosedHandler;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.Divider;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.gwt.HTMLPanel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Quiz;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.TeamSearch;
import org.sagebionetworks.web.client.presenter.ProjectFilterEnum;
import org.sagebionetworks.web.client.presenter.SettingsPresenter;
import org.sagebionetworks.web.client.presenter.SortOptionEnum;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.FitImage;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.header.Header.MenuItems;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.TeamListWidget;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProfileViewImpl extends Composite implements ProfileView {

	public interface ProfileViewImplUiBinder extends UiBinder<Widget, ProfileViewImpl> {}
	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	
	@UiField
	 Div viewProfilePanel;
	 @UiField
	 Image certificationBadge;
	 @UiField
	 Heading displayNameField;
	 @UiField
	 Heading headlineField;
	 @UiField
	 Paragraph industryLocationField;
	 @UiField
	 Paragraph summaryField;
	 @UiField
	 org.gwtbootstrap3.client.ui.Anchor urlField;
	 @UiField
	 TextBox synapseEmailField;
	@UiField
	Button editProfileButton;
	@UiField
	Button importLinkedIn;
	@UiField
	SimplePanel editUserProfilePanel;
	
	@UiField
	SimplePanel picturePanel;
	
	//////Tabs
	@UiField
	Anchor projectsLink;
	@UiField
	LIElement projectsListItem;
	@UiField
	Anchor teamsLink;
	@UiField
	LIElement teamsListItem;
	@UiField
	Anchor settingsLink;
	@UiField
	LIElement settingsListItem;
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
	DivElement projectsTabContainer;
	@UiField
	DivElement challengesTabContainer;
	@UiField
	DivElement teamsTabContainer;
	@UiField
	DivElement settingsTabContainer;
	
	//Project tab
	//filters
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
	TextBox createProjectTextBox;
	@UiField
	Button createProjectButton;
	@UiField
	DivElement createProjectUI;
	@UiField
	FlowPanel projectsTabContent;
	@UiField
	SimplePanel emptyProjectUI;
	@UiField
	Button moreProjectsButton;
	
	//Headings
	@UiField
	Heading projectsHeading;
	@UiField
	Heading teamsHeading;
	@UiField
	Heading challengesHeading;
	
	
	//Project tab
	@UiField
	Button projectSortButton;
	@UiField
	DropDownMenu sortProjectsDropDownMenu;	
	
	//Teams tab
	@UiField
	TextBox createTeamTextBox;
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
		
	//Challenges
	@UiField
	FlowPanel challengesTabContent;
	@UiField
	Button moreChallengesButton;

	//Settings
	@UiField
	FlowPanel settingsTabContent;
	
	
	//highlight boxes
	@UiField 
	DivElement projectsHighlightBox;
	@UiField 
	DivElement challengesHighlightBox;
	@UiField 
	DivElement teamsHighlightBox;

	@UiField 
	DivElement projectsLoadingUI;
	@UiField 
	DivElement challengesLoadingUI;
	@UiField 
	Row profilePictureLoadingUI;
	
	@UiField
	FlowPanel favoritesHelpPanel;
	@UiField
	Button showProfileButton;
	@UiField
	Button hideProfileButton;
	@UiField
	Alert getCertifiedAlert;
	
	@UiField
	SimplePanel profileSynAlertPanel;
	@UiField
	FlowPanel projectSynAlertPanel;
	@UiField
	SimplePanel teamSynAlertPanel;
	@UiField
	FocusPanel alertFocusPanel;
	
	private Presenter presenter;
	private Header headerWidget;
	private SageImageBundle sageImageBundle;
	
	//View profile widgets
	private static HTML defaultProfilePicture = new HTML(DisplayUtils.getFontAwesomeIcon("user font-size-150 lightGreyText"));
	
	private Footer footerWidget;
	private SynapseJSNIUtils synapseJSNIUtils;
	private OpenTeamInvitationsWidget openInvitesWidget;
	private SettingsPresenter settingsPresenter;
	
	@Inject
	public ProfileViewImpl(ProfileViewImplUiBinder binder,
			Header headerWidget, 
			Footer footerWidget, 
			SageImageBundle sageImageBundle,
			SynapseJSNIUtils synapseJSNIUtils, 
			OpenTeamInvitationsWidget openInvitesWidget, 
			SettingsPresenter settingsPresenter) {		
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.sageImageBundle = sageImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.openInvitesWidget = openInvitesWidget;
		this.settingsPresenter = settingsPresenter;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		headerWidget.setMenuItemActive(MenuItems.PROJECTS);
		picturePanel.clear();
		initTabs();
		createProjectTextBox.getElement().setAttribute("placeholder", DisplayConstants.NEW_PROJECT_NAME);
		createProjectButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.createProject(createProjectTextBox.getValue());
			}
		});
		createProjectTextBox.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					createProjectButton.click();
				}
			}
		});
		createTeamTextBox.getElement().setAttribute("placeholder", DisplayConstants.NEW_TEAM_NAME);
		createTeamButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.createTeam(createTeamTextBox.getValue());
			}
		});
		
		createTeamTextBox.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					createTeamButton.click();
				}
			}
		});
		
		teamSearchButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.goTo(new TeamSearch(""));
			}
		});
		
		projectSearchButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.goTo(new Search(""));
			}
		});
		
		alertFocusPanel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.goTo(new Quiz("Certification"));
			}
		});

		initCertificationBadge();

		moreProjectsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.getMoreProjects();
			}
		});
		showProjectsLoading(false);
		
		moreChallengesButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.getMoreChallenges();
			}
		});
		showChallengesLoading(false);
		
		favoritesFilter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.applyFilterClicked(ProjectFilterEnum.FAVORITES, null);
			}
		});
		allProjectsFilter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.applyFilterClicked(ProjectFilterEnum.ALL, null);
			}
		});
		myProjectsFilter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.applyFilterClicked(ProjectFilterEnum.MINE, null);
			}
		});
		sharedDirectlyWithMeFilter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.applyFilterClicked(ProjectFilterEnum.MY_PARTICIPATED_PROJECTS, null);
			}
		});		
		
		showProfileButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.showProfileButtonClicked();
			}
		});
		hideProfileButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.hideProfileButtonClicked();
			}
		});
		editProfileButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				presenter.onEditProfile();
			}
		});
		importLinkedIn.addClickHandler(new ClickHandler() {
	
			@Override
			public void onClick(ClickEvent event) {
				presenter.onImportLinkedIn();
			}
		});
		getCertifiedAlert.addClosedHandler(new AlertClosedHandler() {
			@Override
			public void onClosed(AlertClosedEvent evt) {
				presenter.setGetCertifiedDismissed();
			}
		});
		synapseEmailField.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				synapseEmailField.selectAll();
			}
		});

	}
	
	@Override
	public void addMyTeamsWidget(TeamListWidget myTeamsWidget) {
		teamsTabContent.clear();
		teamsTabContent.add(myTeamsWidget.asWidget());
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
	public void setTeamSynAlertWidget(Widget teamSynAlert) {
		teamSynAlertPanel.setWidget(teamSynAlert);
	}
	
	public void clearSortOptions() {
		sortProjectsDropDownMenu.clear();
	}
	
	public void setSortText(String text) {
		projectSortButton.setText(text);
	}
	
	public void addSortOption(final SortOptionEnum sortOption) {
		final AnchorListItem newSortOption = new AnchorListItem(sortOption.sortText);
		newSortOption.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.resort(sortOption);
			}	
		});
		sortProjectsDropDownMenu.add(newSortOption);
	}
	
	private void initCertificationBadge() {
		certificationBadge.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				clear();
				presenter.certificationBadgeClicked();
			}
		});
	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		header.clear();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
	}
	
	@Override 
	public void setProjectSortVisible(boolean isVisible) {
		projectSortButton.setVisible(isVisible);
	}
	
	@Override
	public void setFavoritesHelpPanelVisible(boolean isVisible) {
		favoritesHelpPanel.setVisible(isVisible);
	}
	
	@Override
	public void setTeamNotificationCount(String count) {
		teamsLink.setHTML(DisplayConstants.TEAMS + "&nbsp" + DisplayUtils.getBadgeHtml(count));
	}
	
	@Override
	public void setProfile(UserProfile profile, boolean isOwner) {
		viewProfilePanel.setVisible(true);
		fillInProfileView(profile);
		picturePanel.add(getProfilePicture(profile, synapseJSNIUtils));
		if (!isOwner) {
			setHighlightBoxUser(DisplayUtils.getDisplayName(profile));
		}
	}
	
	@Override
	public void showTabs(boolean isOwner) {
		DisplayUtils.hide(settingsListItem);
		openInvitesContainer.setVisible(isOwner);
		if (isOwner) {
			resetHighlightBoxes();
			DisplayUtils.show(settingsListItem);
			settingsTabContent.add(settingsPresenter.asWidget());
			//show create project and team UI
			DisplayUtils.show(createProjectUI);
			DisplayUtils.show(createTeamUI);
		}		
		//Teams
		DisplayUtils.show(navtabContainer);
	}
	
	@Override
	public void addCertifiedBadge() {
		certificationBadge.setVisible(true);
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
				presenter.applyFilterClicked(ProjectFilterEnum.MY_TEAM_PROJECTS, null);
			}
		});
		teamFiltersDropDownMenu.add(teamFilter);
		teamFiltersDropDownMenu.add(new Divider());
		
	}
	
	@Override
	public void setTeamsFilterVisible(boolean isVisible) {
		teamFilters.setVisible(isVisible);	
	}
	
	@Override
	public void setTeamsError(String error) {
		DisplayUtils.showErrorMessage(error);
	}

	@Override
	public void setProjectsError(String error) {
		DisplayUtils.showErrorMessage(error);
	}
	
	@Override
	public void setEmptyProjectUIVisible(boolean b) {
		emptyProjectUI.setVisible(b);
	}
	
	@Override
	public void clearProjects() {
		projectsTabContent.clear();
		setIsMoreProjectsVisible(false);
		setEmptyProjectUIVisible(false);
		favoritesHelpPanel.setVisible(false);
	}
	@Override
	public void clearChallenges() {
		challengesTabContent.clear();
		setIsMoreChallengesVisible(false);
	}
	
	@Override
	public void setIsMoreProjectsVisible(boolean isVisible) {
		moreProjectsButton.setVisible(isVisible);
	}
	
	@Override
	public void addProjectWidget(Widget toAdd) {
		toAdd.addStyleName("margin-bottom-10 col-xs-12");
		projectsTabContent.add(toAdd);
	}
	
	@Override
	public void addChallengeWidget(Widget toAdd) {
		DisplayUtils.show(challengesListItem);
		toAdd.addStyleName("margin-top-10");
		challengesTabContent.add(toAdd);
	}
	
	@Override
	public void showChallengesLoading(boolean isVisible) {
		UIObject.setVisible(challengesLoadingUI, isVisible);
	}
	
	@Override
	public void setIsMoreChallengesVisible(boolean isVisible) {
		moreChallengesButton.setVisible(isVisible);
	}
	@Override
	public void setChallengesError(String error) {
		DisplayUtils.showErrorMessage(error);
	}
	
	public static Widget getProfilePicture(UserProfile profile, SynapseJSNIUtils synapseJSNIUtils) {
		 Widget profilePicture; 
		 if (profile.getProfilePicureFileHandleId() != null) {
			 //use preview
			 String url = DisplayUtils.createUserProfileAttachmentUrl(synapseJSNIUtils.getBaseProfileAttachmentUrl(), profile.getOwnerId(), profile.getProfilePicureFileHandleId(), false);
			 profilePicture = new FitImage(url, 150, 150);
		 } else {
			 //use default picture
			 profilePicture = defaultProfilePicture;
		 }
		 profilePicture.addStyleName("margin-10 userProfileImage");
		 return profilePicture;
	 }
	 
	 public void fillInProfileView(UserProfile profile) {
		 fillInProfileView(profile.getFirstName(), profile.getLastName(), profile.getUserName(), profile.getIndustry(), profile.getLocation(), profile.getSummary(), profile.getCompany(), profile.getPosition(), profile.getUrl());
	 }
	 
	 public void fillInProfileView(String fName, String lName, String userName, String industry, String location, String summary, String company, String position, String url) {
		 String name = DisplayUtils.getDisplayName(fName, lName, userName);
		 url = DisplayUtils.replaceWithEmptyStringIfNull(url);
		 company = DisplayUtils.replaceWithEmptyStringIfNull(company);
		 position = DisplayUtils.replaceWithEmptyStringIfNull(position);
		 industry = DisplayUtils.replaceWithEmptyStringIfNull(industry);
		 location = DisplayUtils.replaceWithEmptyStringIfNull(location);
		 summary = DisplayUtils.replaceWithEmptyStringIfNull(summary);
		 
		 //build profile html
		 displayNameField.setText(name);
		 String atString = position.length()>0 && company.length()>0 ? " at " : "";
		 headlineField.setText(position + atString + company);
		 SafeHtmlBuilder builder = new SafeHtmlBuilder();
		 builder.appendEscapedLines(industry);
		 if (location.length()>0) { 
			 builder.appendHtmlConstant(" | ");
	 	 }
		 if (location.length()>0) {
			 builder.appendEscapedLines(location);
		 }
		 industryLocationField.setHTML(builder.toSafeHtml().asString());
		 builder = new SafeHtmlBuilder();
		 builder.appendEscapedLines(summary);
		 summaryField.setHTML(builder.toSafeHtml().asString());
		 urlField.setText(url);
		 urlField.setHref(url);
		 synapseEmailField.setText(userName+"@synapse.org");
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
	public void showLoading() {
		profilePictureLoadingUI.setVisible(true);
	}

	@Override
	public void hideLoading() {
		profilePictureLoadingUI.setVisible(false);
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showProjectsLoading(boolean isLoading) {
		UIObject.setVisible(projectsLoadingUI, isLoading);
	}
	
	@Override
	public void clear() {
		certificationBadge.setVisible(false);
		viewProfilePanel.setVisible(false);
		picturePanel.clear();
		DisplayUtils.hide(navtabContainer);
		clearProjects();
		//init with loading widget
		projectsTabContent.add(new HTMLPanel(DisplayUtils.getLoadingHtml(sageImageBundle)));
		
		settingsTabContent.clear();
		
		challengesTabContent.clear();
		hideTabContainers();
		getCertifiedAlert.setVisible(false);
		DisplayUtils.hide(createProjectUI);
		DisplayUtils.hide(createTeamUI);
		DisplayUtils.hide(challengesListItem);
		createTeamTextBox.setValue("");
		createProjectTextBox.setValue("");
		
		//reset tab link text (remove any notifications)
		clearTeamNotificationCount();
		projectFiltersUI.setVisible(false);
		teamFiltersDropDownMenu.clear();
	}
	
	@Override
	public void clearTeamNotificationCount() {
		teamsLink.setHTML(DisplayConstants.TEAMS);
	}
	
	private void hideTabContainers() {
		//hide all tab containers
		DisplayUtils.hide(projectsTabContainer);
		DisplayUtils.hide(challengesTabContainer);
		DisplayUtils.hide(teamsTabContainer);
		DisplayUtils.hide(settingsTabContainer);
	}
	
	
	/**
	 * Used only for setting the view's tab display
	 * @param targetTab
	 * @param userSelected 
	 */
	@Override
	public void setTabSelected(Synapse.ProfileArea targetTab) {
		// tell presenter what tab we're on only if the user clicked
		if(targetTab == null) targetTab = Synapse.ProfileArea.PROJECTS; // select tab, set default if needed
		hideTabContainers();
		removeClass("active", projectsListItem, teamsListItem, settingsListItem, challengesListItem);
		
		if (targetTab == Synapse.ProfileArea.PROJECTS) {
			setTabSelected(projectsListItem, projectsTabContainer);
		} else if(targetTab == Synapse.ProfileArea.TEAMS) {
			setTabSelected(teamsListItem, teamsTabContainer);
		} else if(targetTab == Synapse.ProfileArea.SETTINGS) {
			setTabSelected(settingsListItem, settingsTabContainer);
		} else if (targetTab == Synapse.ProfileArea.CHALLENGES) {
			setTabSelected(challengesListItem, challengesTabContainer);
		} else {
			showErrorMessage("Unrecognized profile tab: " + targetTab.name());
			return;
		}
		presenter.updateArea(targetTab);
	}
	
	private void setTabSelected(LIElement listItem, DivElement container) {
		//only selects if the list item is visible
		if (UIObject.isVisible(listItem)) {
			listItem.addClassName("active");
			DisplayUtils.show(container);
		} else {
			//if tab is not visible, select projects tab
			projectsListItem.addClassName("active");
			DisplayUtils.show(projectsTabContainer);
		}
	}
	
	private void removeClass(String cssClassName, LIElement... elements) {
		for (LIElement liElement : elements) {
			liElement.removeClassName(cssClassName);
		}
	}
	
	private void initTabs() {
		projectsLink.addClickHandler(getTabClickHandler(Synapse.ProfileArea.PROJECTS));
		teamsLink.addClickHandler(getTabClickHandler(Synapse.ProfileArea.TEAMS));
		settingsLink.addClickHandler(getTabClickHandler(Synapse.ProfileArea.SETTINGS));
		challengesLink.addClickHandler(getTabClickHandler(Synapse.ProfileArea.CHALLENGES));
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
	public void showConfirmDialog(
			String title, 
			String message,
			Callback yesCallback
			) {
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
	public void setHideProfileButtonVisible(boolean isVisible) {
		hideProfileButton.setVisible(isVisible);
	}
	
	@Override
	public void setShowProfileButtonVisible(boolean isVisible) {
		showProfileButton.setVisible(isVisible);
	}
	
	@Override
	public void showProfile() {
		UIObject.setVisible(profileUI, true);
		dashboardUI.addClassName("col-md-7");
	}
	
	@Override
	public void hideProfile() {
		UIObject.setVisible(profileUI, false);
		dashboardUI.removeClassName("col-md-7");
	}
	
	@Override
	public void setProfileEditButtonVisible(boolean isVisible) {
		this.editProfileButton.setVisible(isVisible);
		this.importLinkedIn.setVisible(isVisible);
	}

	@Override
	public void addUserProfileModalWidget(IsWidget userProfileModalWidget) {
		this.editUserProfilePanel.clear();
		this.editUserProfilePanel.add(userProfileModalWidget);
	}
	
	@Override
	public void setGetCertifiedVisible(boolean isVisible) {
		getCertifiedAlert.setVisible(isVisible);	
	}
	@Override
	public void setSynapseEmailVisible(boolean isVisible) {
		synapseEmailField.setVisible(isVisible);
	}
}
