package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAttachmentDialog;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.header.Header.MenuItems;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProfileViewImpl extends Composite implements ProfileView {

	public interface ProfileViewImplUiBinder extends UiBinder<Widget, ProfileViewImpl> {}
	private static final int COLUMN_FORM_WIDTH = 600;
	
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
	SimplePanel editProfileButtonPanel;
	@UiField
	SimplePanel editPhotoButtonPanel;
	@UiField
	public SimplePanel profilePicturePanel;
	@UiField
	SimplePanel breadcrumbsPanel;
	
	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private Header headerWidget;
	private SageImageBundle sageImageBundle;
	private FormPanel userFormPanel;
	private HorizontalPanel linkedInPanel;
	private HorizontalPanel editProfileCommandPanel;
	private Button updateUserInfoButton;
	private Button cancelUpdateUserButton;
	private Button linkedInButton;
	private Anchor linkedInImportLink;
	private Button editProfileButton;
	private Anchor editPhotoLink;
	private Breadcrumb breadcrumb;
	
	//Edit profile form fields
	private TextField<String> firstName;
	private TextField<String> lastName;
	private TextField<String> position;
	private TextField<String> company;
	private TextField<String> industry;
	private TextArea summary;
	private TextField<String> location;
	
	//View profile widgets
	private Html profileWidget;
	private Image defaultProfilePicture;
	private Html profilePictureHtml;
	
	private HandlerRegistration editPhotoHandler = null;

	private static final String PICTURE_CANVAS_ID = "pictureCanvasId";

	private String baseProfileAttachmentUrl = GWT.getModuleBaseURL()+"profileAttachment";
	
	@Inject
	public ProfileViewImpl(ProfileViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget, IconsImageBundle icons,
			SageImageBundle imageBundle, SageImageBundle sageImageBundle,Breadcrumb breadcrumb) {		
		initWidget(binder.createAndBindUi(this));

		this.iconsImageBundle = icons;
		this.headerWidget = headerWidget;
		this.sageImageBundle = sageImageBundle;
		this.breadcrumb = breadcrumb;
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		headerWidget.setMenuItemActive(MenuItems.PROJECTS);
	}


	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;		
		headerWidget.refresh();				
		Window.scrollTo(0, 0); // scroll user to top of page
	}
	
	@Override
	public void updateView(UserProfile profile, boolean isEditing, boolean isOwner) {
		//when editable, show profile form and linkedin import ui
		clear();
		if (isEditing)
		{
		    updateUserForm(profile);
		 	updateUserInfoPanel.add(userFormPanel);
		 	updateWithLinkedInPanel.add(linkedInPanel);
		}
		else
		{
			//view only
			//if isOwner, show Edit button too (which redirects to the edit version of the Profile place)
			updateViewProfile(profile);
			viewProfilePanel.add(profileWidget);
			if (isOwner) {
				editPhotoButtonPanel.add(editPhotoLink);
				editProfileButtonPanel.add(editProfileCommandPanel);
			}
				
		}
	}
	
	@Override
	public void render() {
		//set the Settings page breadcrumb
		breadcrumbsPanel.clear();
		breadcrumbsPanel.add(breadcrumb.asWidget("Profile"));
				
		createViewProfile();
		createProfileForm();
		
		createLinkedInPanel();
		
	    createEditProfileCommandsPanel();
		
	    editPhotoLink = new Anchor();
	    editPhotoLink.addStyleName("user-profile-change-photo");
	    editPhotoLink.setText("Edit Photo");
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
	    
		editProfileCommandPanel.add(linkedInPanel);
		editProfileCommandPanel.add(editProfileButton);
		editProfileCommandPanel.setCellWidth(editProfileButton, "15%");
	}
	 
	private void createLinkedInPanel() {
		linkedInPanel = new HorizontalPanel();
		linkedInImportLink = new Anchor();
		linkedInImportLink.addStyleName("user-profile-linkedin");
		linkedInImportLink.setText("Import from ");
		linkedInButton = new Button();
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
	}
	
	private void linkedInClicked()
	{
		linkedInButton.disable();
		presenter.redirectToLinkedIn();
	}
	
	@Override
	public void showUserUpdateSuccess() {
		updateUserInfoButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.checkGreen16()));
		updateUserInfoButton.setText("Profile Updated");
		presenter.redirectToViewProfile();
	}

	@Override
	public void userUpdateFailed() {
		updateUserInfoButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.error16()));
		updateUserInfoButton.setText("Profile Update Failed");
	}

	 private void createProfileForm() {
		 userFormPanel = new FormPanel();
		 FormData formData = new FormData("-20");
		 
	     userFormPanel.setFrame(true);
	     userFormPanel.setHeaderVisible(false); 
	     userFormPanel.setLabelAlign(LabelAlign.TOP);
	     userFormPanel.setSize(COLUMN_FORM_WIDTH, -1);
	     
	     LayoutContainer main = new LayoutContainer();
	     main.setLayout(new ColumnLayout());

	     LayoutContainer left = new LayoutContainer();
	     left.setStyleAttribute("paddingRight", "10px");
	     FormLayout layout = new FormLayout();
	     layout.setLabelAlign(LabelAlign.TOP);
	     left.setLayout(layout);
	     
	     LayoutContainer right = new LayoutContainer();
	     right.setStyleAttribute("paddingLeft", "10px");
	     layout = new FormLayout();
	     layout.setLabelAlign(LabelAlign.TOP);
	     right.setLayout(layout);
	     
	     firstName = new TextField<String>();  
	     firstName.setFieldLabel("First Name");  
	     firstName.setAllowBlank(false);
	     left.add(firstName, formData);
	   
	     lastName = new TextField<String>();  
	     lastName.setFieldLabel("Last Name");  
	     lastName.setAllowBlank(false);
	     right.add(lastName, formData);
	     
	     position = new TextField<String>();  
	     position.setFieldLabel("Current Position");  
	     position.setAllowBlank(true);
	     left.add(position, formData);
	     
	     company = new TextField<String>();  
	     company.setFieldLabel("Current Company");  
	     company.setAllowBlank(true);
	     right.add(company, formData);
	     
	     industry = new TextField<String>();  
	     industry.setFieldLabel("Industry");  
	     industry.setAllowBlank(true);
	     left.add(industry, formData);
	     
	     location = new TextField<String>();  
	     location.setFieldLabel("Location");  
	     location.setAllowBlank(true);
	     right.add(location, formData);
	     
	     main.add(left, new ColumnData(.5));
	     main.add(right, new ColumnData(.5));
	     
	     summary = new TextArea();  
	     summary.setFieldLabel("Summary");  
	     summary.setAllowBlank(true);
	     summary.setHeight(200);
	     
	     userFormPanel.add(main, new FormData("100%"));
	     userFormPanel.add(summary, new FormData("100%"));
	     
	     userFormPanel.setButtonAlign(HorizontalAlignment.LEFT);  
	     updateUserInfoButton = new Button(DisplayConstants.BUTTON_CHANGE_USER_INFO);
	     updateUserInfoButton.addSelectionListener(new SelectionListener<ButtonEvent>() {				
	    	 @Override
	    	 public void componentSelected(ButtonEvent ce) {
	    		 if(firstName.getValue().trim().equals("") || firstName.getValue().trim() == null) {
	    			 MessageBox.alert("Error", "Please enter your first name.", null);
	    		 } else if(lastName.getValue().trim().equals("") || lastName.getValue() == null) {
	    			 MessageBox.alert("Error", "Please enter your last name.", null);
	    		 } else {
	    			 DisplayUtils.changeButtonToSaving(updateUserInfoButton, sageImageBundle);
	    			 presenter.updateProfile(firstName.getValue(), lastName.getValue(), summary.getValue(), position.getValue(), location.getValue(), industry.getValue(), company.getValue(), null, null);
	    		 }
		
	    	 }
	     });
	     cancelUpdateUserButton = new Button(DisplayConstants.BUTTON_CANCEL);
	     cancelUpdateUserButton.addSelectionListener(new SelectionListener<ButtonEvent>() {				
	    	 @Override
	    	 public void componentSelected(ButtonEvent ce) {
	    		  presenter.redirectToViewProfile();
	    	 }
	     });	     
	     
	     setUpdateUserInfoDefaultIcon();
	     userFormPanel.addButton(updateUserInfoButton);
	     userFormPanel.addButton(cancelUpdateUserButton);
	     // Enter key used to submit.  with a multiline component in the mix, we lose this convenience (unless we add code to
	     //	know if the summary field is in focus, or have a way of asking for the currently selected component (via the focus manager?)
//	     new KeyNav<ComponentEvent>(userFormPanel) {
//	    	 @Override
//	    	 public void onEnter(ComponentEvent ce) {
//	    		 super.onEnter(ce);
//	    		 if (updateUserInfoButton.isEnabled())
//	    			 updateUserInfoButton.fireEvent(Events.Select);
//	    	 }
//	     };

	     // form binding so submit button is greyed out until all fields are filled 
	     final FormButtonBinding binding = new FormButtonBinding(userFormPanel);
	     binding.addButton(updateUserInfoButton);

	 }
	 
	 private void updateUserForm(UserProfile profile) {
		 firstName.setValue(profile.getFirstName());
		 lastName.setValue(profile.getLastName());
		 position.setValue(profile.getPosition());
		 company.setValue(profile.getCompany());

		 industry.setValue(profile.getIndustry());
		 summary.setValue(profile.getSummary());
		 location.setValue(profile.getLocation());
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
		 String givenName = fixIfNullString(profile.getFirstName());
		 String familyName = fixIfNullString(profile.getLastName());
		 if (givenName.length() > 0 && familyName.length()>0)
			 name = givenName + " " + familyName;
		 else
			 name = profile.getDisplayName();
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
		 

		 profileWidget.setHtml(builder.toSafeHtml().asString());
		 
		 if (profile.getPic() != null && profile.getPic().getPreviewId() != null && profile.getPic().getPreviewId().length() > 0)
		 {
			 profilePicturePanel.add(profilePictureHtml);
			 profilePictureHtml.setHtml(SafeHtmlUtils.fromSafeConstant("<div class=\"profile-image-loading\" >"
			    		+ "<img style=\"margin:auto; display:block;\" src=\"" 
			    		+ DisplayUtils.createUserProfileAttachmentUrl(baseProfileAttachmentUrl, profile.getOwnerId(), profile.getPic().getPreviewId(), null) + "\"/>"
			    		+ "</div>").asString());
		 }
		 else
		 {
			 profilePicturePanel.add(defaultProfilePicture);
		 }
		 setElementStyle(PICTURE_CANVAS_ID, Display.BLOCK);
		 
		 String userId = profile.getOwnerId();
		 final String actionUrl =  baseProfileAttachmentUrl+ "?" + DisplayUtils.USER_PROFILE_PARAM_KEY + "=" + userId;
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
	 
	 private void setUpdateUserInfoDefaultIcon() {
		 updateUserInfoButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.arrowCurve16()));
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
		editPhotoButtonPanel.clear();
		profilePicturePanel.clear();
		setElementStyle(PICTURE_CANVAS_ID, Display.NONE);
	}
	
	private void setElementStyle(String id, Style.Display display){
		Element pictureCanvasElement = DOM.getElementById(id);
		if (pictureCanvasElement != null && pictureCanvasElement.getStyle() != null)
			pictureCanvasElement.getStyle().setDisplay(display);
	}
}
