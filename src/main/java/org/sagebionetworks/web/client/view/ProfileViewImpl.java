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
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Synapse;
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
	SimplePanel updateWithLinkedInPanel;
	@UiField
	SimplePanel viewProfilePanel;
	
	@UiField
	FlowPanel editProfileButtonPanel;
	@UiField
	SimplePanel breadcrumbsPanel;
	@UiField
	SimplePanel picturePanel;
	@UiField
	SimplePanel editPicturePanel;
	@UiField
	SimplePanel editPictureButtonPanel;
	
	@UiField
	Anchor showProfileLink;
	
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
//	@UiField
//	Anchor messagesLink;
//	@UiField
//	LIElement messagesListItem;
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
	FlowPanel messagesTabContainer;
	@UiField
	FlowPanel settingsTabContainer;
	
	
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
	
	//Challenges
	@UiField
	FlowPanel challengesTabContent;
	
	//Favorites
	@UiField
	FlowPanel favoritesTabContent;
	
	private Presenter presenter;
	private Header headerWidget;
	private SageImageBundle sageImageBundle;
	private Button linkedInButtonEditProfile;
	private Button editProfileButton;
	private Breadcrumb breadcrumb;
	
	//View profile widgets
	private FlowPanel profileWidget;
	private HTML defaultProfilePicture;
	
	private Footer footerWidget;
	private CookieProvider cookies;
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
			CookieProvider cookies,
			CertificateWidget certificateWidget,
			SettingsPresenter settingsPresenter,
			PortalGinInjector ginInjector) {		
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.sageImageBundle = sageImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.breadcrumb = breadcrumb;
		this.openInvitesWidget = openInvitesWidget;
		this.myTeamsWidget = myTeamsWidget;
		this.cookies = cookies;
		this.certificateWidget = certificateWidget;
		this.settingsPresenter = settingsPresenter;
		this.ginInjector = ginInjector;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		headerWidget.setMenuItemActive(MenuItems.PROJECTS);
		certificatePanel.setWidget(certificateWidget.asWidget());
		createViewProfile();
		linkedInButtonEditProfile = createLinkedInButton();
		
		createEditProfileCommandsPanel();
		
		picturePanel.clear();
		initTabs();
		
		showProfileLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.show(viewProfilePanel);
				DisplayUtils.show(picturePanel);
				DisplayUtils.hide(showProfileLink);
			}
		});
		
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
		projectsTabContent.addStyleName("margin-10");
		challengesTabContent.addStyleName("margin-10");
		favoritesTabContent.addStyleName("margin-10");
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
	public void updateView(UserProfile profile, boolean isEditing, boolean isOwner, PassingRecord passingRecord, Widget profileFormWidget) {
		clear();
		//when editable, show profile form and linkedin import ui
		if (isEditing)
		{
			Widget profilePicture = getProfilePicture(profile, profile.getPic());
			profilePicture.addStyleName("left");
			editPicturePanel.add(profilePicture);
			editPictureButtonPanel.add(getEditPictureButton(profile));
			updateUserInfoPanel.add(profileFormWidget);
		 	updateWithLinkedInPanel.add(linkedInButtonEditProfile);
		}
		else
		{
			//view only
			teamsTabContent.clear();
			settingsTabContainer.clear();
			
			DisplayUtils.hide(settingsListItem);
//			DisplayUtils.hide(messagesListItem);
			
			updateViewProfile(profile, passingRecord);
			viewProfilePanel.add(profileWidget);
			
			if (isOwner) {
				DisplayUtils.show(favoritesListItem);
				DisplayUtils.show(showProfileLink);
				DisplayUtils.show(settingsListItem);
				settingsTabContainer.add(settingsPresenter.asWidget());
//				DisplayUtils.show(messagesListItem);
				
				//if owner, show Edit button too (which redirects to the edit version of the Profile place)
				editProfileButtonPanel.add(editProfileButton);
				openInvitesWidget.configure(new Callback() {
					@Override
					public void invoke() {
						//refresh the teams and invites
						presenter.refreshTeams();
					}
				}, (CallbackP)null);
				
				teamsTabContent.add(openInvitesWidget.asWidget());
				
				//hide my profile by default, and provide link to show it
				DisplayUtils.hide(viewProfilePanel);
				DisplayUtils.hide(picturePanel);
				
				//show create project and team UI
				DisplayUtils.show(createProjectUI);
				DisplayUtils.show(createTeamUI);
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
			
			setTabSelected(ProfileArea.PROJECTS);
			DisplayUtils.show(navtabContainer);
		}
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
	
	@Override
	public void render() {
		//set the Settings page breadcrumb
		breadcrumbsPanel.clear();
		breadcrumbsPanel.add(breadcrumb.asWidget("Profile"));
	}

	private void createEditProfileCommandsPanel() {
		editProfileButton = DisplayUtils.createIconButton(DisplayConstants.BUTTON_EDIT_PROFILE, ButtonType.DEFAULT, "glyphicon-pencil");
		editProfileButton.addClickHandler(new ClickHandler() {
	    	@Override
			public void onClick(ClickEvent event) {
	    		presenter.showEditProfile();
	    	}
	    });
		editProfileButton.addStyleName("right btn-xs margin-left-5");
	}
	 
	private Button createLinkedInButton() {
		Button command = DisplayUtils.createIconButton("", ButtonType.DEFAULT, "");
		command.addClickHandler(new ClickHandler() {
	    	@Override
			public void onClick(ClickEvent event) {
				linkedInClicked();
			}
		});
	    
		command.setHTML(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getFontelloIcon("linkedin-squared") + "Import from LinkedIn"));
		command.addStyleName("right btn-xs");
		
	    return command;
	}
	
	private void linkedInClicked()
	{
		presenter.redirectToLinkedIn();
	}
	
	 private void createViewProfile() {
		 profileWidget = new FlowPanel();
		 defaultProfilePicture = new HTML(DisplayUtils.getFontelloIcon("user font-size-150 lightGreyText"));
	 }
	 
	 /**
	  * just return the empty string if input string parameter s is null, otherwise returns s.
	  */
	 private String fixIfNullString(String s)
	 {
		if (s == null)
			return "";
		else return s;
	 }
	 
	 private Widget getProfilePicture(UserProfile profile, AttachmentData pic) {
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
	 
	 private Button getEditPictureButton(final UserProfile profile) {
		 String userId = profile.getOwnerId();
		 final String actionUrl =  synapseJSNIUtils.getBaseProfileAttachmentUrl()+ "?" + WebConstants.USER_PROFILE_PARAM_KEY + "=" + userId;
		 Button editPictureButton = DisplayUtils.createButton("Upload new picture");
		 editPictureButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
	    		//upload a new photo. UI to send to S3, then update the profile with the new attachment data (by redirecting back to view profile)
						AddAttachmentDialog.showAddAttachmentDialog(actionUrl,sageImageBundle, 
								DisplayConstants.ATTACH_PROFILE_PIC_DIALOG_TITLE,
								DisplayConstants.ATTACH_PROFILE_PIC_DIALOG_BUTTON_TEXT,new AddAttachmentDialog.Callback() {
							@Override
							public void onSaveAttachment(UploadResult result) {
								if(result != null){
									if(UploadStatus.SUCCESS == result.getUploadStatus()){
										showInfo(DisplayConstants.TEXT_PROFILE_PICTURE_SUCCESS, "");
										editPicturePanel.clear();
										Widget profilePicture = getProfilePicture(profile, result.getAttachmentData());
										profilePicture.addStyleName("left");
										editPicturePanel.add(profilePicture);
									}else{
										showErrorMessage(DisplayConstants.ERROR_PROFILE_PICTURE_FAILED+result.getMessage());
									}
								}
							}
						});
			}
		});
		return editPictureButton;
	 }
	 
	 private void updateViewProfile(UserProfile profile, PassingRecord passingRecord) {
		 profileWidget.clear();
		 String name, industry, location, summary;
		 name = DisplayUtils.getDisplayName(profile);
		 
		 String company = fixIfNullString(profile.getCompany());
		 String position = fixIfNullString(profile.getPosition());
		 industry = fixIfNullString(profile.getIndustry());
		 location = fixIfNullString(profile.getLocation());
		 summary = fixIfNullString(profile.getSummary());
		 
		 //build profile html
		 SafeHtmlBuilder builder = new SafeHtmlBuilder();
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
			
			profileWidget.add(tutorialLink);
		 }
			 
		 builder.appendHtmlConstant("<h2>");
		 builder.appendEscapedLines(name);
		 builder.appendHtmlConstant("</h2>");
		 
		 HTML headlineHtml = new HTML(builder.toSafeHtml());
		 headlineHtml.addStyleName("inline-block");
		 profileWidget.add(headlineHtml);
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
		 
		 String url = fixIfNullString(profile.getUrl());
		 if (url.length() > 0) {
			 builder.appendHtmlConstant("<p><a href=\""+url+"\" class=\"link\" target=\"_blank\">" + url + "</a></p>");
		 }
		 
//		 // Account number
//		 builder.appendHtmlConstant("<h5>" + DisplayConstants.SYNAPSE_ACCOUNT_NUMBER + ": ").appendEscaped(profile.getOwnerId()).appendHtmlConstant("</h5>");		 
		 
		 HTML profileHtml = new HTML(builder.toSafeHtml());
		 profileWidget.add(profileHtml);

		 picturePanel.add(getProfilePicture(profile, profile.getPic()));
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
		updateWithLinkedInPanel.clear();
		updateUserInfoPanel.clear();
		viewProfilePanel.clear();
		editProfileButtonPanel.clear();
		picturePanel.clear();
		editPicturePanel.clear();
		editPictureButtonPanel.clear();
		certificatePanel.setVisible(false);
		DisplayUtils.hide(navtabContainer);
		projectsTabContent.clear();
		//init with loading widget
		projectsTabContent.add(new HTMLPanel(DisplayUtils.getLoadingHtml(sageImageBundle)));
		
		favoritesTabContent.clear();
		favoritesTabContent.add(new HTMLPanel(DisplayUtils.getLoadingHtml(sageImageBundle)));
		
		DisplayUtils.hide(showProfileLink);
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
		DisplayUtils.hide(messagesTabContainer);
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
		projectsListItem.removeClassName("active");
		projectsLink.addStyleName("link");
		teamsListItem.removeClassName("active");
		teamsLink.addStyleName("link");
		settingsListItem.removeClassName("active");
		settingsLink.addStyleName("link");
		challengesListItem.removeClassName("active");
		challengesLink.addStyleName("link");
//		messagesListItem.removeClassName("active");
//		messagesLink.addStyleName("link");
		favoritesListItem.removeClassName("active");
		favoritesLink.addStyleName("link");

		
		LIElement tab; 
		Anchor link;
		
		if (targetTab == Synapse.ProfileArea.PROJECTS) {
			tab = projectsListItem;
			link = projectsLink;
			DisplayUtils.show(projectsTabContainer);
		} else if(targetTab == Synapse.ProfileArea.TEAMS) {
			tab = teamsListItem;
			link = teamsLink;
			DisplayUtils.show(teamsTabContainer);
		} else if(targetTab == Synapse.ProfileArea.SETTINGS) {
			tab = settingsListItem;
			link = settingsLink;
			DisplayUtils.show(settingsTabContainer);
		} else if (targetTab == Synapse.ProfileArea.CHALLENGES) {
			tab = challengesListItem;
			link = challengesLink;
			DisplayUtils.show(challengesTabContainer);
		} else if (targetTab == Synapse.ProfileArea.FAVORITES) {
			tab = favoritesListItem;
			link = favoritesLink;
			DisplayUtils.show(favoritesTabContainer);
//		} else if(targetTab == Synapse.ProfileArea.MESSAGES) {
//			tab = messagesListItem;
//			link = messagesLink;
//			DisplayUtils.show(messagesTabContainer);
		} else {
			showErrorMessage("Unrecognized profile tab: " + targetTab.name());
			return;
		}
		
		link.removeStyleName("link");
		tab.addClassName("active");
	}
	
	private void initTabs() {
		projectsLink.setText(DisplayConstants.PROJECTS);
		projectsLink.addClickHandler(getTabClickHandler(Synapse.ProfileArea.PROJECTS));
		
		teamsLink.setText(DisplayConstants.TEAMS);
		teamsLink.addClickHandler(getTabClickHandler(Synapse.ProfileArea.TEAMS));
		
		settingsLink.setText(DisplayConstants.SETTINGS);
		settingsLink.addClickHandler(getTabClickHandler(Synapse.ProfileArea.SETTINGS));
		
		challengesLink.setText(DisplayConstants.CHALLENGES);
		challengesLink.addClickHandler(getTabClickHandler(Synapse.ProfileArea.CHALLENGES));
		
		favoritesLink.setText(DisplayConstants.FAVORITES);
		favoritesLink.addClickHandler(getTabClickHandler(Synapse.ProfileArea.FAVORITES));
//		messagesLink.setText(DisplayConstants.MESSAGES);
//		messagesLink.addClickHandler(getTabClickHandler(Synapse.ProfileArea.MESSAGES));
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
