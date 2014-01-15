package org.sagebionetworks.web.client.view;

import java.util.List;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAttachmentDialog;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.header.Header.MenuItems;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.TeamListWidget;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
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
	SimplePanel updateWithLinkedInPanel;
	@UiField
	SimplePanel viewProfilePanel;
	@UiField
	SimplePanel myTeamsPanel;
	@UiField
	SimplePanel myTeamInvitesPanel;
	
	@UiField
	SimplePanel notificationsPanel;
	@UiField
	CheckBox emailNotificationsCheckbox;

	@UiField
	SimplePanel editProfileButtonPanel;
	@UiField
	SimplePanel breadcrumbsPanel;
	@UiField
	SimplePanel pictureCanvasPanel;
	
	private LayoutContainer pictureCanvasContainer;
	private LayoutContainer profilePictureContainer;
	private LayoutContainer editPhotoButtonContainer;
	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private Header headerWidget;
	private SageImageBundle sageImageBundle;
	private FormPanel userFormPanel;
	private HorizontalPanel linkedInPanelForViewProfile;
	private HorizontalPanel linkedInPanelForEditProfile;
	private HorizontalPanel editProfileCommandPanel;
	private Button editProfileButton;
	private Anchor editPhotoLink;
	private Breadcrumb breadcrumb;
	
	//View profile widgets
	private Html profileWidget;
	private Image defaultProfilePicture;
	private Html profilePictureHtml;
	
	private HandlerRegistration editPhotoHandler = null;

	private Footer footerWidget;
	private CookieProvider cookies;
	private SynapseJSNIUtils synapseJSNIUtils;
	private OpenTeamInvitationsWidget openInvitesWidget;
	private TeamListWidget myTeamsWidget;
	
	@Inject
	public ProfileViewImpl(ProfileViewImplUiBinder binder,
			Header headerWidget, 
			Footer footerWidget, 
			IconsImageBundle icons,
			SageImageBundle imageBundle, 
			SageImageBundle sageImageBundle,
			Breadcrumb breadcrumb, 
			SynapseJSNIUtils synapseJSNIUtils, 
			OpenTeamInvitationsWidget openInvitesWidget, 
			TeamListWidget myTeamsWidget,
			CookieProvider cookies) {		
		initWidget(binder.createAndBindUi(this));

		this.iconsImageBundle = icons;
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.sageImageBundle = sageImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.breadcrumb = breadcrumb;
		this.openInvitesWidget = openInvitesWidget;
		this.myTeamsWidget = myTeamsWidget;
		this.cookies = cookies;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		headerWidget.setMenuItemActive(MenuItems.PROJECTS);
		
		createViewProfile();
		linkedInPanelForViewProfile = createLinkedInPanel();
		linkedInPanelForEditProfile = createLinkedInPanel();
		
		createEditProfileCommandsPanel();
		
	    editPhotoLink = new Anchor();
	    editPhotoLink.addStyleName("user-profile-change-photo");
	    editPhotoLink.setText("Edit Photo");
	    pictureCanvasContainer = new LayoutContainer();
	    pictureCanvasContainer.setStyleName("inner-6 view notopmargin");
	    pictureCanvasPanel.clear();
	    pictureCanvasPanel.add(pictureCanvasContainer);
	    
	    profilePictureContainer = new LayoutContainer();
	    profilePictureContainer.addStyleName("center");
		editPhotoButtonContainer = new LayoutContainer();
		editPhotoButtonContainer.setStyleName("center notopmargin");
		
		pictureCanvasContainer.add(profilePictureContainer);
		pictureCanvasContainer.add(editPhotoButtonContainer);
		ClickHandler notificationsClickHandler = getNotificationsClickHandler();
		emailNotificationsCheckbox.addClickHandler(notificationsClickHandler);
	}
	
	private ClickHandler getNotificationsClickHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//update notification settings
				presenter.updateMyNotificationSettings(emailNotificationsCheckbox.getValue(), false);
			}
		};
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
	public void updateView(UserProfile profile, List<Team> teams, boolean isEditing, boolean isOwner, Widget profileFormWidget) {
		clear();
		//when editable, show profile form and linkedin import ui
		if (isEditing)
		{
			updateUserInfoPanel.add(profileFormWidget);
		 	updateWithLinkedInPanel.add(linkedInPanelForEditProfile);
		}
		else
		{
			//view only
			myTeamsWidget.configure(teams, false);
			myTeamsPanel.add(myTeamsWidget.asWidget());
			myTeamsPanel.setVisible(true);
		
			//if isOwner, show Edit button too (which redirects to the edit version of the Profile place)
			updateViewProfile(profile);
			viewProfilePanel.add(profileWidget);
			notificationsPanel.setVisible(isOwner);
			if (isOwner) {
				editPhotoButtonContainer.add(editPhotoLink);
				editPhotoButtonContainer.layout();
				editProfileButtonPanel.add(editProfileCommandPanel);
				openInvitesWidget.configure(new Callback() {
					@Override
					public void invoke() {
						//refresh the view after accepting/ignoring
						presenter.redirectToViewProfile();
					}
				});
				
				myTeamInvitesPanel.add(openInvitesWidget.asWidget());
				
				boolean isNotify = true;
				if(profile.getNotificationSettings() != null) {
					isNotify = profile.getNotificationSettings().getSendEmailNotifications();
				}
				emailNotificationsCheckbox.setValue(isNotify, false);
			}
		}
	}
	
	@Override
	public void render() {
		//set the Settings page breadcrumb
		breadcrumbsPanel.clear();
		breadcrumbsPanel.add(breadcrumb.asWidget("Profile"));
	}

	private void createEditProfileCommandsPanel() {
		editProfileCommandPanel = new HorizontalPanel();
		
		editProfileButton = new Button(DisplayConstants.BUTTON_EDIT, AbstractImagePrototype.create(iconsImageBundle.editGrey16()));
	    editProfileButton.setHeight(25);
	    editProfileButton.setBorders(false);
	    editProfileButton.addSelectionListener(new SelectionListener<ButtonEvent>() {				
	    	@Override
	    	public void componentSelected(ButtonEvent ce) {
	    		presenter.redirectToEditProfile();
	    	}
	    });
	    
		editProfileCommandPanel.add(linkedInPanelForViewProfile);
		editProfileCommandPanel.add(editProfileButton);
		editProfileCommandPanel.setCellWidth(editProfileButton, "15%");
	}
	 
	private HorizontalPanel createLinkedInPanel() {
		HorizontalPanel linkedInPanel = new HorizontalPanel();
		Anchor linkedInImportLink = new Anchor();
		linkedInImportLink.addStyleName("user-profile-linkedin");
		linkedInImportLink.setText("Import from ");
		Button linkedInButton = new Button();
	    linkedInButton.setIcon(AbstractImagePrototype.create(sageImageBundle.linkedinsmall()));
	    linkedInButton.setSize(sageImageBundle.linkedinsmall().getWidth() + 1, sageImageBundle.linkedinsmall().getHeight() + 1);
	    linkedInButton.setBorders(false);
	    linkedInButton.addSelectionListener(new SelectionListener<ButtonEvent>() {				
	    	@Override
	    	public void componentSelected(ButtonEvent ce) {
	    		linkedInClicked();
	    	}
	    });
	    
	    linkedInImportLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				linkedInClicked();
			}
		});
	    
	    linkedInPanel.add(linkedInImportLink);
	    linkedInPanel.add(linkedInButton);
	    return linkedInPanel;
	}
	
	private void linkedInClicked()
	{
		presenter.redirectToLinkedIn();
	}
	
	 private void createViewProfile() {
		 profileWidget = new Html();
		 profilePictureHtml = new Html();
		 defaultProfilePicture = new Image(sageImageBundle.defaultProfilePicture());
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
	 
	 private void updateViewProfile(UserProfile profile) {
		 String name, industry, location, summary;
		 name = DisplayUtils.getDisplayName(profile);
		 
		 String company = fixIfNullString(profile.getCompany());
		 String position = fixIfNullString(profile.getPosition());
		 industry = fixIfNullString(profile.getIndustry());
		 location = fixIfNullString(profile.getLocation());
		 summary = fixIfNullString(profile.getSummary());
		 
		 //build profile html
		 SafeHtmlBuilder builder = new SafeHtmlBuilder();
		 builder.appendHtmlConstant("<h2>");
		 builder.appendEscapedLines(name);
		 builder.appendHtmlConstant("</h2>");
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
		 

		 profileWidget.setHtml(builder.toSafeHtml().asString());
		 
		 if (profile.getPic() != null && profile.getPic().getPreviewId() != null && profile.getPic().getPreviewId().length() > 0) {
			 profilePictureContainer.add(profilePictureHtml);
			 profilePictureHtml.setHtml(SafeHtmlUtils.fromSafeConstant("<div class=\"profile-image-loading\" >"
			    		+ "<img style=\"margin:auto; display:block;\" src=\"" 
			    		+ DisplayUtils.createUserProfileAttachmentUrl(synapseJSNIUtils.getBaseProfileAttachmentUrl(), profile.getOwnerId(), profile.getPic().getPreviewId(), null) + "\"/>"
			    		+ "</div>").asString());
		 }
		 else {
			 profilePictureContainer.add(defaultProfilePicture);
		 }
		 profilePictureContainer.layout();
		 pictureCanvasContainer.setVisible(true);
		 
		 String userId = profile.getOwnerId();
		 final String actionUrl =  synapseJSNIUtils.getBaseProfileAttachmentUrl()+ "?" + WebConstants.USER_PROFILE_PARAM_KEY + "=" + userId;
		 if (editPhotoHandler != null)
			 editPhotoHandler.removeHandler();
		 editPhotoHandler = editPhotoLink.addClickHandler(new ClickHandler() {
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
									}else{
										showErrorMessage(DisplayConstants.ERROR_PROFILE_PICTURE_FAILED+result.getMessage());
									}
								}
								presenter.redirectToViewProfile();
							}
						});
			}
		});
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
		myTeamInvitesPanel.clear();
		myTeamsPanel.clear();
		myTeamsPanel.setVisible(false);
		editPhotoButtonContainer.removeAll();
		profilePictureContainer.removeAll();
		pictureCanvasContainer.setVisible(false);
		notificationsPanel.setVisible(false);
	}
}
