package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.base.TextBoxBase;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.SynapseJSNIUtils;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProfileFormViewImpl extends Composite implements ProfileFormView {

	public interface ProfileFormImplUiBinder extends UiBinder<Widget, ProfileFormViewImpl> {}
	
	
	private Presenter presenter;
	private SynapseJSNIUtils synapseJSNIUtils;
	@UiField
	SimplePanel fileUploadModalPanel;
	
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
			SynapseJSNIUtils synapseJSNIUtils) {
		initWidget(binder.createAndBindUi(this));
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
		updateProfilePicture(profile);
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
		
		KeyPressHandler changeHandler = new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				presenter.startEditing();
			}
		};
		
		addValueChangeHandler(changeHandler, firstNameField, lastNameField, userNameField, currentPositionField, 
				currentAffiliationField, industryField, locationField, moreInfoField, bioField);
	 }
	 
	 private void addValueChangeHandler(KeyPressHandler changeHandler, TextBoxBase ... textBoxes) {
		for (TextBoxBase textBox : textBoxes) {
			textBox.addKeyPressHandler(changeHandler);
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
		 Button editPictureButton = DisplayUtils.createButton("Upload new picture");
		 editPictureButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onUploadImage();
			}
		});
		return editPictureButton;
	 }
	
	@Override
	public void updateProfilePicture(UserProfile profile) {
		//update main edit picture panel
		editPicturePanel.clear();
		Widget profilePicture = ProfileViewImpl.getProfilePicture(profile, synapseJSNIUtils);
		profilePicture.addStyleName("left");
		editPicturePanel.add(profilePicture);
		//and preview
		picturePanel.clear();
		profilePicture = ProfileViewImpl.getProfilePicture(profile, synapseJSNIUtils);
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

	@Override
	public void addFileInputWidget(IsWidget widget) {
		fileUploadModalPanel.add(widget);
	}
}
