package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.html.Text;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SettingsViewImpl extends Composite implements SettingsView {

	public interface SettingsViewImplUiBinder extends UiBinder<Widget, SettingsViewImpl> {}

	
	@UiField
	DivElement changeSynapsePasswordUI;
	@UiField
	DivElement changeSynapsePasswordHighlightBox;
	@UiField
	DivElement apiKeyHighlightBox;
	@UiField
	DivElement editProfilePanel;
	@UiField
	Panel apiKeyUI;
	@UiField
	org.gwtbootstrap3.client.ui.Button editProfileButton;
	
	@UiField
	FlowPanel forgotPasswordContainer;
	Anchor forgotPasswordLink;
	
	@UiField
	DivElement emailSettingsPanel;
	@UiField
	DivElement changeEmailUI;
	@UiField
	FlowPanel emailsPanel;
	@UiField
	TextBox newEmailField;
	@UiField
	DivElement newEmailError;
	@UiField
	DivElement newEmailAlert;
	@UiField
	Button addEmailButton;

	
	@UiField
	PasswordTextBox currentPasswordField;
	@UiField
	PasswordTextBox password1Field;
	@UiField
	PasswordTextBox password2Field;
	
	@UiField
	DivElement currentPassword;
	@UiField
	DivElement password1;
	@UiField
	DivElement password2;
	@UiField
	DivElement currentPasswordError;
	@UiField
	DivElement password1Error;
	@UiField
	DivElement password2Error;
	
	@UiField
	Button changePasswordBtn;
	
	@UiField
	SpanElement storageUsageSpan;
	@UiField
	TextBox apiKeyContainer;
	
	@UiField
	HTMLPanel notificationsPanel;
	@UiField
	CheckBox emailNotificationsCheckbox;
	@UiField
	org.gwtbootstrap3.client.ui.Button changeApiKey;
	@UiField
	org.gwtbootstrap3.client.ui.Button showApiKey;
	
	@UiField
	SimplePanel addressSynAlertPanel;
	@UiField
	SimplePanel notificationSynAlertPanel;
	@UiField
	SimplePanel apiSynAlertPanel;
	
	private Presenter presenter;
	
	@Inject
	public SettingsViewImpl(SettingsViewImplUiBinder binder) {		
		initWidget(binder.createAndBindUi(this));
		
		ClickHandler notificationsClickHandler = getNotificationsClickHandler();
		emailNotificationsCheckbox.addClickHandler(notificationsClickHandler);
		
		changePasswordBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//validate passwords are filled in and match
				if(checkCurrentPassword() && checkPassword1() && checkPassword2() && checkPasswordMatch()) {
					changePasswordBtn.setEnabled(false);
					presenter.resetPassword(currentPasswordField.getValue(), password1Field.getValue());
				}
			}
		});
		
		changeApiKey.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.changeApiKey();
			}
		});
		
		showApiKey.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.getAPIKey();
			}
		});
		
		forgotPasswordLink = new Anchor();
		forgotPasswordLink.addStyleName("link movedown-4 margin-left-10");
		forgotPasswordLink.setText(DisplayConstants.FORGOT_PASSWORD);
		forgotPasswordLink.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.goTo(new PasswordReset(ClientProperties.DEFAULT_PLACE_TOKEN));				
			}
		});
		forgotPasswordContainer.addStyleName("inline-block");
		forgotPasswordContainer.add(forgotPasswordLink);
		emailSettingsPanel.setAttribute(WebConstants.HIGHLIGHT_BOX_TITLE, "Email Address");
		notificationsPanel.getElement().setAttribute(WebConstants.HIGHLIGHT_BOX_TITLE, "Email Settings");
		changeSynapsePasswordHighlightBox.setAttribute(WebConstants.HIGHLIGHT_BOX_TITLE, "Change Synapse Password");
		apiKeyHighlightBox.setAttribute(WebConstants.HIGHLIGHT_BOX_TITLE, "Synapse API Key");
		editProfilePanel.setAttribute(WebConstants.HIGHLIGHT_BOX_TITLE, "Profile");
		
		newEmailField.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					addEmailButton.click();
				}
			}
		});
		addEmailButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (checkEmailFormat()) {
					addEmailButton.setEnabled(false);
					presenter.addEmail(newEmailField.getValue());
				}
			}
		});
		editProfileButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onEditProfile();
			}
		});
		
		apiKeyContainer.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				apiKeyContainer.selectAll();
			}
		});
	}


	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;		
		Window.scrollTo(0, 0); // scroll user to top of page
	}
	
	@Override
	public void render() {
		currentPasswordField.getElement().setAttribute("placeholder", "Enter current password");
		password1Field.getElement().setAttribute("placeholder", "Enter new password");
		password2Field.getElement().setAttribute("placeholder", "Confirm new password");
		newEmailField.getElement().setAttribute("placeholder", "Enter new email address");
		clear();
	}

	@Override
	public void showPasswordChangeSuccess() {
		resetChangePasswordUI();
		showInfo("Password has been successfully changed", "");
	}

	@Override
	public void passwordChangeFailed(String error) {
		changePasswordBtn.setEnabled(true);
		currentPasswordError.setInnerHTML(error);
		DisplayUtils.showFormError(currentPassword, currentPasswordError);
	}


	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void clearStorageUsageUI() {
		storageUsageSpan.setInnerText(DisplayConstants.STORAGE_USAGE_FAILED_TEXT);
	}
	
	@Override
	public void updateStorageUsage(Long grandTotal) {
		if (grandTotal == null){
			clearStorageUsageUI();
		}
		else {
			storageUsageSpan.setInnerText("You are currently using " + DisplayUtils.getFriendlySize(grandTotal.doubleValue(), false));
		}
	}
	
	public void showNotificationEmailAddress(String primaryEmail) {
		emailsPanel.clear();
		emailsPanel.add(new InlineHTML("<h5 class=\"displayInline\">" + SafeHtmlUtils.htmlEscape(primaryEmail) + "</h5>"));
		final Anchor changeEmail = new Anchor("change");
		changeEmail.addStyleName("link margin-left-10");
		emailsPanel.add(changeEmail);
		changeEmail.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				changeEmail.setVisible(false);
				DisplayUtils.show(changeEmailUI);
			}
		});
	}
	
	@Override
	public void updateNotificationCheckbox(UserProfile profile) {
		boolean isNotify = true;
		if(profile.getNotificationSettings() != null) {
			if (profile.getNotificationSettings().getSendEmailNotifications() != null)
				isNotify = profile.getNotificationSettings().getSendEmailNotifications();
		}
		emailNotificationsCheckbox.setValue(isNotify, false);
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
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	private boolean checkCurrentPassword() {
		DisplayUtils.hideFormError(currentPassword, currentPasswordError);
		if (!DisplayUtils.isDefined(currentPasswordField.getText())){
			currentPasswordError.setInnerHTML(DisplayConstants.ERROR_ALL_FIELDS_REQUIRED);
			DisplayUtils.showFormError(currentPassword, currentPasswordError);
			return false;
		} else
			return true;
	}
	
	private boolean checkPassword1() {
		DisplayUtils.hideFormError(password1, password1Error);
		if (!DisplayUtils.isDefined(password1Field.getText())){
			password1Error.setInnerHTML(DisplayConstants.ERROR_ALL_FIELDS_REQUIRED);
			DisplayUtils.showFormError(password1, password1Error);
			return false;
		} else
			return true;
	}
	
	private boolean checkPassword2() {
		DisplayUtils.hideFormError(password2, password2Error);
		if (!DisplayUtils.isDefined(password2Field.getText())){
			password2Error.setInnerHTML(DisplayConstants.ERROR_ALL_FIELDS_REQUIRED);
			DisplayUtils.showFormError(password2, password2Error);
			return false;
		} else
			return true;
	}
	
	private boolean checkPasswordMatch() {
		DisplayUtils.hideFormError(password2, password2Error);
		if (!password1Field.getValue().equals(password2Field.getValue())) {
			password2Error.setInnerHTML(DisplayConstants.PASSWORDS_MISMATCH);
			DisplayUtils.showFormError(password2, password2Error);
			return false;
		} else
			return true;
	}
	
	@Override
	public void clear() {
		resetChangePasswordUI();
		storageUsageSpan.setInnerText("");
		resetAddEmailUI();
	}
	
	@Override
	public void showEmailChangeFailed(String error) {
		addEmailButton.setEnabled(true);
		DisplayUtils.show(newEmailError);
		newEmailError.setInnerHTML(error);
	}
	
	private void resetAddEmailUI() {
		emailsPanel.clear();
		newEmailField.setValue("");
		DisplayUtils.hide(newEmailError);
		addEmailButton.setEnabled(true);
		DisplayUtils.hide(changeEmailUI);
		DisplayUtils.hide(newEmailAlert);
	}
	
	private void resetChangePasswordUI() {
		currentPasswordField.setValue("");
		password1Field.setValue("");
		password2Field.setValue("");
		changePasswordBtn.setEnabled(true);
		DisplayUtils.hideFormError(currentPassword, currentPasswordError);
		DisplayUtils.hideFormError(password1, password1Error);
		DisplayUtils.hideFormError(password2, password2Error);
	}

	private boolean checkEmailFormat(){
		DisplayUtils.hide(newEmailError);
		if (LoginPresenter.isValidEmail(newEmailField.getValue())) {
			return true;
		}
		else {
			DisplayUtils.show(newEmailError);
			newEmailError.setInnerHTML(WebConstants.INVALID_EMAIL_MESSAGE);
			return false;
		}
	}
	
	@Override
	public void showEmailChangeSuccess(String message) {
		DisplayUtils.hide(changeEmailUI);
		DisplayUtils.show(newEmailAlert);
		newEmailAlert.setInnerHTML(message);
	}

	@Override
	public void setApiKey(String apiKey) {
		apiKeyContainer.setText(apiKey);
		apiKeyUI.setVisible(true);
		changeApiKey.setVisible(true);
		showApiKey.setVisible(false);
	}


	@Override
	public void setNotificationSynAlertWidget(Widget notificationSynAlert) {
		notificationSynAlertPanel.setWidget(notificationSynAlert);
	}


	@Override
	public void setAddressSynAlertWidget(Widget addressSynAlert) {
		addressSynAlertPanel.setWidget(addressSynAlert);
	}

	@Override
	public void setAPISynAlertWidget(Widget apiSynAlert) {
		apiSynAlertPanel.setWidget(apiSynAlert);
	}
	
	@Override
	public void hideAPIKey() {
		apiKeyUI.setVisible(false);
		changeApiKey.setVisible(false);
		showApiKey.setVisible(true);
	}
	
	@Override
	public void showConfirm(String message, ConfirmCallback callback) {
		Bootbox.confirm(message, callback);
	}
}
