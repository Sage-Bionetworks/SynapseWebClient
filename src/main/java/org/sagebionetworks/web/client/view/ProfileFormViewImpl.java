package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProfileFormViewImpl extends Composite implements ProfileFormView {

	public interface ProfileFormImplUiBinder extends UiBinder<Widget, ProfileFormViewImpl> {}
	
	
	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private SageImageBundle sageImageBundle;
	
	@UiField
	Button okButton;
	@UiField
	Button cancelButton;
	
	@UiField
	InputElement firstNameField;
	@UiField
	InputElement lastNameField;
	@UiField
	InputElement userNameField;
	
	@UiField
	InputElement currentPositionField;
	@UiField
	InputElement currentAffiliationField;
	@UiField
	InputElement industryField;
	@UiField
	InputElement locationField;
	@UiField
	InputElement moreInfoField;
	@UiField
	TextAreaElement bioField;
	
	@Inject
	public ProfileFormViewImpl(ProfileFormImplUiBinder binder, 
			IconsImageBundle icons,
			SageImageBundle sageImageBundle) {
		initWidget(binder.createAndBindUi(this));
		this.iconsImageBundle = icons;
		this.sageImageBundle = sageImageBundle;
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
		okButton.removeStyleName("disabled");
		okButton.setHTML(text);
	}
	
	 private void init() {
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				startSave();
			}
		});
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				presenter.cancelClicked();
			}
		});
	 }
	 
	 private void startSave() {
		 DisplayUtils.changeButtonToSaving(okButton);
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
		 userNameField.setValue(profile.getUserName());
	 }
		 

	 @Override
	 public void hideCancelButton(){
		 cancelButton.setVisible(false);
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
	
	@Override
	
	public void clear() {
	}
}
