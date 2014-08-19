package org.sagebionetworks.web.client.view;

import java.util.List;

import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.gwt.HTMLPanel;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.TeamSearch;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.presenter.SettingsPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.FitImage;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.EntityBadge;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowserViewImpl;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAttachmentDialog;
import org.sagebionetworks.web.client.widget.entity.download.CertificateWidget;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.header.Header.MenuItems;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.TeamListWidget;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
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
	SimplePanel updateUserInfoPanel;
	@UiField
	SimplePanel certificatePanel;
	@UiField
	FlowPanel viewProfilePanel;
	
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
	Anchor favoritesLink;
	@UiField
	LIElement favoritesListItem;


	@UiField
	DivElement navtabContainer;
	
	@UiField
	DivElement projectsTabContainer;
	@UiField
	DivElement favoritesTabContainer;
	@UiField
	DivElement challengesTabContainer;
	@UiField
	DivElement teamsTabContainer;
	@UiField
	DivElement settingsTabContainer;
	
	//Project tab
	@UiField
	TextBox createProjectTextBox;
	@UiField
	Button createProjectButton;
	@UiField
	DivElement createProjectUI;
	@UiField
	FlowPanel projectsTabContent;
	
	//Teams tab
	@UiField
	TextBox createTeamTextBox;
	@UiField
	Button createTeamButton;
	@UiField
	DivElement createTeamUI;
	@UiField
	FlowPanel teamsTabContent;
	@UiField
	Button teamSearchButton;
	
	//Challenges
	@UiField
	FlowPanel challengesTabContent;
	
	//Favorites
	@UiField
	FlowPanel favoritesTabContent;
	
	//Settings
	@UiField
	FlowPanel settingsTabContent;
	
	private Presenter presenter;
	private Header headerWidget;
	private SageImageBundle sageImageBundle;
	
	//View profile widgets
	private static HTML defaultProfilePicture = new HTML(DisplayUtils.getFontelloIcon("user font-size-150 lightGreyText"));
	
	private Footer footerWidget;
	private SynapseJSNIUtils synapseJSNIUtils;
	private OpenTeamInvitationsWidget openInvitesWidget;
	private TeamListWidget myTeamsWidget;
	private CertificateWidget certificateWidget;
	private SettingsPresenter settingsPresenter;
	private PortalGinInjector ginInjector;
	
	@Inject
	public ProfileViewImpl(ProfileViewImplUiBinder binder,
			Header headerWidget, 
			Footer footerWidget, 
			SageImageBundle sageImageBundle,
			Breadcrumb breadcrumb, 
			SynapseJSNIUtils synapseJSNIUtils, 
			OpenTeamInvitationsWidget openInvitesWidget, 
			TeamListWidget myTeamsWidget,
			CertificateWidget certificateWidget,
			SettingsPresenter settingsPresenter,
			PortalGinInjector ginInjector) {		
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.sageImageBundle = sageImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.openInvitesWidget = openInvitesWidget;
		this.myTeamsWidget = myTeamsWidget;
		this.certificateWidget = certificateWidget;
		this.settingsPresenter = settingsPresenter;
		this.ginInjector = ginInjector;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		headerWidget.setMenuItemActive(MenuItems.PROJECTS);
		certificatePanel.setWidget(certificateWidget.asWidget());
		
		picturePanel.clear();
		initTabs();
		
		createProjectTextBox.getElement().setAttribute("placeholder", DisplayConstants.NEW_PROJECT_NAME);
		createProjectButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.createProject(createProjectTextBox.getValue());
			}
		});
		
		createTeamTextBox.getElement().setAttribute("placeholder", DisplayConstants.NEW_TEAM_NAME);
		createTeamButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.createTeam(createTeamTextBox.getValue());
			}
		});
		
		teamSearchButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.goTo(new TeamSearch(""));
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
	public void updateView(UserProfile profile, boolean isOwner, PassingRecord passingRecord, Widget profileFormWidget, ProfileArea initialTab) {
		clear();
		//when editable, show profile form and linkedin import ui
		teamsTabContent.clear();
		DisplayUtils.hide(settingsListItem);
		//add certificate
		if (passingRecord != null) {
			 Image tutorialLink = new Image(sageImageBundle.certificate().getSafeUri());
			 tutorialLink.setHeight("32px");
			 tutorialLink.setWidth("25px");
			 tutorialLink.setPixelSize(25, 32);
			 tutorialLink.addStyleName("imageButton margin-right-5 moveup-8");
			 certificateWidget.configure(profile, passingRecord);
			 final Tooltip tooltip = DisplayUtils.addTooltip(tutorialLink.asWidget(), DisplayConstants.CERTIFIED_USER);
			 tutorialLink.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					clear();
					certificatePanel.setVisible(true);
					tooltip.hide();
				}
			});
			
			 viewProfilePanel.add(tutorialLink);
		 }
		
		fillInProfileView(profile, viewProfilePanel);
		picturePanel.add(getProfilePicture(profile, profile.getPic(), synapseJSNIUtils));
		
		if (isOwner) {
			DisplayUtils.show(favoritesListItem);
			DisplayUtils.show(settingsListItem);
			
			openInvitesWidget.configure(new Callback() {
				@Override
				public void invoke() {
					//refresh the teams and invites
					presenter.refreshTeams();
				}
			}, (CallbackP)null);
			
			teamsTabContent.add(openInvitesWidget.asWidget());
			
			settingsTabContent.add(settingsPresenter.asWidget());
			
			//hide my profile by default, and provide link to show it
			DisplayUtils.hide(viewProfilePanel);
			DisplayUtils.hide(picturePanel);
			
			//show create project and team UI
			DisplayUtils.show(createProjectUI);
			DisplayUtils.show(createTeamUI);
			
			initEditProfileUI(profile, profileFormWidget);
		} else {
			DisplayUtils.show(viewProfilePanel);
			DisplayUtils.show(picturePanel);
		}
		
		//Teams
		SimplePanel wrapper = new SimplePanel();
		wrapper.add(myTeamsWidget.asWidget());
		wrapper.addStyleName("highlight-box");
		wrapper.getElement().setAttribute(WebConstants.HIGHLIGHT_BOX_TITLE, "Teams");
		teamsTabContent.add(wrapper);
		
		if (initialTab != null)
			setTabSelected(initialTab);
		else 
			setTabSelected(ProfileArea.PROJECTS);
		DisplayUtils.show(navtabContainer);
	}
	
	private void initEditProfileUI(UserProfile profile, Widget profileFormWidget){
		updateUserInfoPanel.add(profileFormWidget);
	}
	
	@Override
	public void setTeams(List<Team> teams) {
		myTeamsWidget.configure(teams, true);
	}
	
	@Override
	public void setTeamsError(String error) {
		DisplayUtils.showErrorMessage(error);
	}
	
	@Override
	public void setProjects(List<EntityHeader> projectHeaders) {
		addEntityBadges(projectHeaders, projectsTabContent);
	}

	@Override
	public void setProjectsError(String error) {
		DisplayUtils.showErrorMessage(error);
	}
	
	private void addEntityBadges(List<EntityHeader> projectHeaders, FlowPanel targetPanel) {
		targetPanel.clear();
		for (EntityHeader entityHeader : projectHeaders) {
			EntityBadge badge = ginInjector.getEntityBadgeWidget();
			badge.configure(entityHeader);
			Widget widget = badge.asWidget();
			widget.addStyleName("margin-top-5");
			targetPanel.add(widget);
		}
		if (projectHeaders.isEmpty())
			targetPanel.add(new HTML(SafeHtmlUtils.fromSafeConstant("<div class=\"smallGreyText padding-15\">" + EntityTreeBrowserViewImpl.PLACEHOLDER_NAME_PREFIX + " " + DisplayConstants.EMPTY + "</div>").asString()));
	}
	
	@Override
	public void setChallenges(List<EntityHeader> projectHeaders) {
		if (projectHeaders.size() > 0) {
			DisplayUtils.show(challengesListItem);
			addEntityBadges(projectHeaders, challengesTabContent);
		}
	}
	
	@Override
	public void setChallengesError(String error) {
		DisplayUtils.showErrorMessage(error);
	}
	
	@Override
	public void setFavorites(List<EntityHeader> headers) {
		addEntityBadges(headers, favoritesTabContent);
	}
	
	@Override
	public void setFavoritesError(String error) {
		DisplayUtils.showErrorMessage(error);
	}
	
	 public static Widget getProfilePicture(UserProfile profile, AttachmentData pic, SynapseJSNIUtils synapseJSNIUtils) {
		 if (pic != null && pic.getPreviewId() != null && pic.getPreviewId().length() > 0) {
			 //use preview
			 String url = DisplayUtils.createUserProfileAttachmentUrl(synapseJSNIUtils.getBaseProfileAttachmentUrl(), profile.getOwnerId(), profile.getPic().getPreviewId(), null);
			 return new HTML(SafeHtmlUtils.fromSafeConstant("<div class=\"profile-image-loading\" >"
					 + "<img style=\"margin:auto; display:block;\" src=\"" 
					 + url+ "\"/>"
					 + "</div>"));
		 } else if (pic != null && pic.getTokenId() != null && pic.getTokenId().length() > 0) {
			 //use token
			 String url = DisplayUtils.createUserProfileAttachmentUrl(synapseJSNIUtils.getBaseProfileAttachmentUrl(), profile.getOwnerId(), pic.getTokenId(), null);
			 return new FitImage(url, 150, 150);
		 }
		 else {
			 //use default picture
			 return defaultProfilePicture;
		 }
	 }
	 
	 public static void fillInProfileView(UserProfile profile, FlowPanel viewProfilePanel) {
		 fillInProfileView(profile.getFirstName(), profile.getLastName(), profile.getUserName(), profile.getIndustry(), profile.getLocation(), profile.getSummary(), profile.getCompany(), profile.getPosition(), profile.getUrl(), viewProfilePanel);
	 }
	 
	 public static void fillInProfileView(String fName, String lName, String userName, String industry, String location, String summary, String company, String position, String url, FlowPanel viewProfilePanel) {
		 String name = DisplayUtils.getDisplayName(fName, lName, userName);
		 url = DisplayUtils.replaceWithEmptyStringIfNull(url);
		 company = DisplayUtils.replaceWithEmptyStringIfNull(company);
		 position = DisplayUtils.replaceWithEmptyStringIfNull(position);
		 industry = DisplayUtils.replaceWithEmptyStringIfNull(industry);
		 location = DisplayUtils.replaceWithEmptyStringIfNull(location);
		 summary = DisplayUtils.replaceWithEmptyStringIfNull(summary);
		 
		 //build profile html
		 SafeHtmlBuilder builder = new SafeHtmlBuilder();
		  builder.appendHtmlConstant("<h2>");
		 builder.appendEscapedLines(name);
		 builder.appendHtmlConstant("</h2>");
		 
		 HTML headlineHtml = new HTML(builder.toSafeHtml());
		 headlineHtml.addStyleName("inline-block");
		 viewProfilePanel.add(headlineHtml);
		 builder = new SafeHtmlBuilder();
		 
		 if (position.length()>0 || company.length()>0) {
			 builder.appendHtmlConstant("<h4 class=\"user-profile-headline\">");
			 String atString = position.length()>0 && company.length()>0 ? " at " : "";
			 builder.appendEscapedLines(position + atString + company);
			 builder.appendHtmlConstant("</h4>");
		 }
		 
		 builder.appendHtmlConstant("<p class=\"user-profile-industry-location\">");
		 
		 if (industry.length()>0) {
			 builder.appendEscapedLines(industry);
			 if (location.length()>0) 
				 builder.appendHtmlConstant(" | ");
		 }
		 if (location.length()>0) {
			 builder.appendEscapedLines(location);
		 }
		 builder.appendHtmlConstant("</p>");
			
		 
		 if (summary.length()>0) {
			 builder.appendHtmlConstant("<p class=\"user-profile-summary\">");
			 builder.appendEscapedLines(summary);
			 builder.appendHtmlConstant("</p>");
		 }
		 
		 if (url.length() > 0) {
			 builder.appendHtmlConstant("<p><a href=\""+url+"\" class=\"link\" target=\"_blank\">" + url + "</a></p>");
		 }
		 
		 HTML profileHtml = new HTML(builder.toSafeHtml());
		 viewProfilePanel.add(profileHtml);
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
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		updateUserInfoPanel.clear();
		viewProfilePanel.clear();
		picturePanel.clear();
		certificatePanel.setVisible(false);
		DisplayUtils.hide(navtabContainer);
		projectsTabContent.clear();
		//init with loading widget
		projectsTabContent.add(new HTMLPanel(DisplayUtils.getLoadingHtml(sageImageBundle)));
		
		favoritesTabContent.clear();
		favoritesTabContent.add(new HTMLPanel(DisplayUtils.getLoadingHtml(sageImageBundle)));
		
		settingsTabContent.clear();
		
		challengesTabContent.clear();
		hideTabContainers();
		DisplayUtils.hide(createProjectUI);
		DisplayUtils.hide(createTeamUI);
		DisplayUtils.hide(challengesListItem);
		DisplayUtils.hide(favoritesListItem);
	}
	
	private void hideTabContainers() {
		//hide all tab containers
		DisplayUtils.hide(projectsTabContainer);
		DisplayUtils.hide(favoritesTabContainer);
		DisplayUtils.hide(challengesTabContainer);
		DisplayUtils.hide(teamsTabContainer);
		DisplayUtils.hide(settingsTabContainer);
	}
	
	
	/**
	 * Used only for setting the view's tab display
	 * @param targetTab
	 * @param userSelected 
	 */
	private void setTabSelected(Synapse.ProfileArea targetTab) {
		// tell presenter what tab we're on only if the user clicked
		if(targetTab == null) targetTab = Synapse.ProfileArea.PROJECTS; // select tab, set default if needed
		hideTabContainers();
		removeClass("active", projectsListItem, teamsListItem, settingsListItem, challengesListItem, favoritesListItem);
		
		if (targetTab == Synapse.ProfileArea.PROJECTS) {
			setTabSelected(projectsListItem, projectsTabContainer);
		} else if(targetTab == Synapse.ProfileArea.TEAMS) {
			setTabSelected(teamsListItem, teamsTabContainer);
		} else if(targetTab == Synapse.ProfileArea.SETTINGS) {
			setTabSelected(settingsListItem, settingsTabContainer);
		} else if (targetTab == Synapse.ProfileArea.CHALLENGES) {
			setTabSelected(challengesListItem, challengesTabContainer);
		} else if (targetTab == Synapse.ProfileArea.FAVORITES) {
			setTabSelected(favoritesListItem, favoritesTabContainer);
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
		favoritesLink.addClickHandler(getTabClickHandler(Synapse.ProfileArea.FAVORITES));
	}
	
	private ClickHandler getTabClickHandler(final Synapse.ProfileArea targetTab) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setTabSelected(targetTab);					
			}
		};
	}
}
