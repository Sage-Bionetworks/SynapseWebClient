package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.base.TextBoxBase;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAttachmentDialog;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProfileFormViewImpl extends Composite implements ProfileFormView {

	public interface ProfileFormImplUiBinder extends UiBinder<Widget, ProfileFormViewImpl> {}
	
	
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private SynapseJSNIUtils synapseJSNIUtils;
	
	@UiField
	org.gwtbootstrap3.client.ui.Button okButton;
	@UiField
	org.gwtbootstrap3.client.ui.Button cancelButton;
	@UiField
	org.gwtbootstrap3.client.ui.Button previewButton;
	@UiField
	org.gwtbootstrap3.client.ui.Button previewDialogOkButton;
	
	@UiField
	Button changeUsernameButton;
	
	@UiField
	HeadingElement userNameHeading;
	@UiField
	TextBox firstNameField;
	@UiField
	TextBox lastNameField;
	@UiField
	TextBox userNameField;
	@UiField
	DivElement changeUsernameUi;
	@UiField
	DivElement moreInfoUi;
	@UiField
	DivElement urlError;
	@UiField
	DivElement userNameError;
	
	@UiField
	TextBox currentPositionField;
	@UiField
	TextBox currentAffiliationField;
	@UiField
	TextBox industryField;
	@UiField
	TextBox locationField;
	@UiField
	TextBox moreInfoField;
	@UiField
	TextArea bioField;
	
	@UiField
	SimplePanel updateWithLinkedInPanel;

	@UiField
	SimplePanel editPicturePanel;
	@UiField
	SimplePanel editPictureButtonPanel;

	@UiField
	Modal previewDialog;
	
	@UiField
	FlowPanel viewProfilePanel;
	@UiField
	SimplePanel picturePanel;
	
	
	private Button linkedInButtonEditProfile;
	
	@Inject
	public ProfileFormViewImpl(ProfileFormImplUiBinder binder, 
			SageImageBundle sageImageBundle,
			SynapseJSNIUtils synapseJSNIUtils) {
		initWidget(binder.createAndBindUi(this));
		this.sageImageBundle = sageImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
		linkedInButtonEditProfile = createLinkedInButton();
		init();
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void updateView(UserProfile profile) {
		clear();
		updateUserForm(profile);
		updateProfilePicture(profile, profile.getPic());
		editPictureButtonPanel.add(getEditPictureButton(profile));
		updateWithLinkedInPanel.add(linkedInButtonEditProfile);
	}
	
	@Override
	public void showUserUpdateSuccess() {
		changeUserInfoButtonToDefault();
	}

	@Override
	public void userUpdateFailed() {
		changeUserInfoButtonToDefault();
	}
	
	private void changeUserInfoButtonToDefault()
	{
		setUpdateButtonText(DisplayConstants.BUTTON_CHANGE_USER_INFO);
	}

	@Override
	public void setUpdateButtonText(String text){
		okButton.setEnabled(true);
		okButton.setText(text);
	}
	
	 private void init() {
		okButton.addStyleName("right margin-right-10");
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				startSave();
			}
		});
		
		changeUsernameButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showChangeUsernameUI();
			}
		});
		
		cancelButton.addStyleName("right margin-right-5");
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.rollback();
			}
		});
		
		previewButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				viewProfilePanel.clear();
				String fName, lName, userName, industry, location, summary, company, position, url;
				fName = trim(firstNameField.getValue());
				lName = trim(lastNameField.getValue());
				userName = trim(userNameField.getValue());
				industry = trim(industryField.getValue());
				location = trim(locationField.getValue());
				summary = trim(bioField.getValue());
				company = trim(currentAffiliationField.getValue());
				position =trim(currentPositionField.getValue());
				url = trim(moreInfoField.getValue());
				ProfileViewImpl.fillInProfileView(fName, lName, userName, industry, location, summary, company, position, url, viewProfilePanel);
				previewDialog.show();
			}
		});
		
		previewDialogOkButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				previewDialog.hide();
			}
		});
		DisplayUtils.hide(changeUsernameUi);
		//initialize as 
		setIsDataModified(false);
		
		ValueChangeHandler<String> changeHandler = new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				presenter.startEditing();
			}
		};
		
		addValueChangeHandler(changeHandler, firstNameField, lastNameField, userNameField, currentPositionField, 
				currentAffiliationField, industryField, locationField, moreInfoField, bioField);
	 }
	 
	 private void addValueChangeHandler(ValueChangeHandler<String> changeHandler, TextBoxBase ... textBoxes) {
		for (TextBoxBase textBox : textBoxes) {
			textBox.addValueChangeHandler(changeHandler);
		}
	 }
	 
	 private void showChangeUsernameUI(){
		changeUsernameButton.addStyleName("disabled");
		DisplayUtils.show(changeUsernameUi);
	 }
	 
	 @Override
	 public void showInvalidUrlUi() {
		 userUpdateFailed();
		 DisplayUtils.showFormError(moreInfoUi, urlError);
	 }
	 
	 @Override
	 public void showInvalidUsernameUi() {
		 userUpdateFailed();
		 DisplayUtils.showFormError(changeUsernameUi, userNameError);
	 }
	 
	 private void startSave() {
		 DisplayUtils.changeButtonToSaving(okButton);
		 DisplayUtils.hideFormError(moreInfoUi, urlError);
		 DisplayUtils.hideFormError(changeUsernameUi, userNameError);
		 presenter.updateProfile(trim(firstNameField.getValue()), trim(lastNameField.getValue()), trim(bioField.getValue()), trim(currentPositionField.getValue()), trim(locationField.getValue()), trim(industryField.getValue()), trim(currentAffiliationField.getValue()), null, null, null, trim(moreInfoField.getValue()), trim(userNameField.getValue()));
	 }
	 
	 private String trim(String value) {
		 if (value == null)
			 return null;
		 else return value.trim();
	 }
	 
	 private void updateUserForm(UserProfile profile) {
		 firstNameField.setValue(profile.getFirstName());
		 lastNameField.setValue(profile.getLastName());
		 currentPositionField.setValue(profile.getPosition());
		 currentAffiliationField.setValue(profile.getCompany());

		 industryField.setValue(profile.getIndustry());
		 bioField.setValue(profile.getSummary());
		 locationField.setValue(profile.getLocation());
		 moreInfoField.setValue(profile.getUrl());
		 if (DisplayUtils.isTemporaryUsername(profile.getUserName())) {
			 userNameField.setValue("");
			 userNameHeading.setInnerText("");
			 showChangeUsernameUI();
			 showInvalidUsernameUi();
			 
		 } else {
			 userNameField.setValue(profile.getUserName());
			 userNameHeading.setInnerText(profile.getUserName());	 
		 }
	 }
		 
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
		changeUserInfoButtonToDefault();
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
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
										updateProfilePicture(profile, result.getAttachmentData());
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
	
	private void updateProfilePicture(UserProfile profile, AttachmentData pic) {
		//update main edit picture panel
		editPicturePanel.clear();
		Widget profilePicture = ProfileViewImpl.getProfilePicture(profile, pic, synapseJSNIUtils);
		profilePicture.addStyleName("left");
		editPicturePanel.add(profilePicture);
		//and preview
		picturePanel.clear();
		profilePicture = ProfileViewImpl.getProfilePicture(profile, pic, synapseJSNIUtils);
		profilePicture.addStyleName("left");
		picturePanel.add(profilePicture);
	}
	
	@Override
	public void clear() {
		updateWithLinkedInPanel.clear();
		editPicturePanel.clear();
		editPictureButtonPanel.clear();
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
		command.addStyleName("right btn-xs margin-right-10 moveup-35");
		
	    return command;
	}
	
	private void linkedInClicked(){
		presenter.redirectToLinkedIn();
	}
	
	@Override
	public void setIsDataModified(boolean isModified) {
		cancelButton.setEnabled(isModified);
		okButton.setEnabled(isModified);
	}
}
