package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Quiz;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WebConstants;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SettingsViewImpl extends Composite implements SettingsView {

	public interface SettingsViewImplUiBinder extends UiBinder<Widget, SettingsViewImpl> {
	}

	@UiField
	Div changeSynapsePasswordUI;
	@UiField
	Div changeSynapsePasswordHighlightBox;
	@UiField
	HTMLPanel apiKeyHighlightBox;
	@UiField
	Div editProfilePanel;
	@UiField
	Panel apiKeyUI;
	@UiField
	org.gwtbootstrap3.client.ui.Button editProfileButton;
	@UiField
	Div dateTimeFormatPanel;
	@UiField
	FlowPanel forgotPasswordContainer;
	Anchor forgotPasswordLink;

	@UiField
	Div emailSettingsPanel;
	@UiField
	Div emailsPanel;

	@UiField
	PasswordTextBox currentPasswordField;
	@UiField
	PasswordTextBox password1Field;
	@UiField
	PasswordTextBox password2Field;

	@UiField
	Row currentPassword;
	@UiField
	Row password1;
	@UiField
	Row password2;
	@UiField
	SimplePanel passwordSynAlertPanel;
	@UiField
	Button changePasswordBtn;

	@UiField
	TextBox apiKeyContainer;

	@UiField
	CheckBox emailNotificationsCheckbox;
	@UiField
	org.gwtbootstrap3.client.ui.Button changeApiKey;
	@UiField
	org.gwtbootstrap3.client.ui.Button showApiKey;

	@UiField
	SimplePanel notificationSynAlertPanel;
	@UiField
	SimplePanel apiSynAlertPanel;
	@UiField
	Div subscriptionsContainer;

	@UiField
	AnchorListItem dateFormatLocal;
	@UiField
	AnchorListItem dateFormatUTC;
	@UiField
	Button dateFormatDropdown;

	// Profile Validation UI
	@UiField
	Div verifyAlert;
	@UiField
	Anchor requestProfileValidationLink1;
	@UiField
	Anchor requestProfileValidationLink2;
	@UiField
	Anchor reviewProfileLink;
	@UiField
	Anchor createOrcIdLink;
	@UiField
	Anchor orcIdField;
	@UiField
	Icon unbindButton;
	@UiField
	Span unbindButtonUI;
	@UiField
	Button linkORCIDButton;
	@UiField
	Button verificationSubmittedButton;
	@UiField
	Button verificationSuspendedButton;
	@UiField
	Button verificationRejectedButton;
	@UiField
	Button resubmitProfileValidationButton;
	@UiField
	Button verificationApprovedButton;
	@UiField
	Button certificationPassedButton;
	@UiField
	Button certificationButton;
	@UiField
	Button synapseTermsAcceptedButton;
	private Presenter presenter;

	@Inject
	public SettingsViewImpl(SettingsViewImplUiBinder binder, final SynapseJSNIUtils jsniUtils) {
		initWidget(binder.createAndBindUi(this));

		ClickHandler notificationsClickHandler = getNotificationsClickHandler();
		emailNotificationsCheckbox.addClickHandler(notificationsClickHandler);

		changePasswordBtn.addClickHandler(event -> {
			presenter.changePassword();
		});

		changeApiKey.addClickHandler(event -> {
			presenter.changeApiKey();
		});

		showApiKey.addClickHandler(event -> {
			presenter.getAPIKey();
		});

		forgotPasswordLink = new Anchor();
		forgotPasswordLink.addStyleName("link movedown-4 margin-left-10");
		forgotPasswordLink.setText(DisplayConstants.FORGOT_PASSWORD);
		forgotPasswordLink.addClickHandler(event -> {
			presenter.goTo(new PasswordReset(ClientProperties.DEFAULT_PLACE_TOKEN));
		});
		forgotPasswordContainer.addStyleName("inline-block");
		forgotPasswordContainer.add(forgotPasswordLink);
		emailSettingsPanel.getElement().setAttribute(WebConstants.HIGHLIGHT_BOX_TITLE, "Email");
		changeSynapsePasswordHighlightBox.getElement().setAttribute(WebConstants.HIGHLIGHT_BOX_TITLE, "Change Synapse Password");
		apiKeyHighlightBox.getElement().setAttribute(WebConstants.HIGHLIGHT_BOX_TITLE, "Synapse API Key");
		editProfilePanel.getElement().setAttribute(WebConstants.HIGHLIGHT_BOX_TITLE, "Profile");
		dateTimeFormatPanel.getElement().setAttribute(WebConstants.HIGHLIGHT_BOX_TITLE, "Date/Time Format");

		ClickHandler editProfileClickHandler = event -> {
			presenter.onEditProfile();
		};
		editProfileButton.addClickHandler(editProfileClickHandler);
		reviewProfileLink.addClickHandler(editProfileClickHandler);

		ClickHandler orcIdClickHandler = event -> presenter.linkOrcIdClicked();
		linkORCIDButton.addClickHandler(orcIdClickHandler);
		createOrcIdLink.addClickHandler(orcIdClickHandler);

		apiKeyContainer.addClickHandler(event -> {
			apiKeyContainer.selectAll();
		});

		dateFormatLocal.addClickHandler(event -> {
			dateFormatDropdown.setText(dateFormatLocal.getText());
			presenter.setShowUTCTime(false);
		});
		dateFormatUTC.addClickHandler(event -> {
			dateFormatDropdown.setText(dateFormatUTC.getText());
			presenter.setShowUTCTime(true);
		});

		ClickHandler newVerificationSubmissionCallback = event -> presenter.newVerificationSubmissionClicked();
		ClickHandler editVerificationSubmissionCallback = event -> presenter.editVerificationSubmissionClicked();

		requestProfileValidationLink1.addClickHandler(newVerificationSubmissionCallback);
		requestProfileValidationLink2.addClickHandler(newVerificationSubmissionCallback);
		verificationApprovedButton.addClickHandler(editVerificationSubmissionCallback);
		verificationSubmittedButton.addClickHandler(editVerificationSubmissionCallback);
		verificationSuspendedButton.addClickHandler(editVerificationSubmissionCallback);
		verificationRejectedButton.addClickHandler(editVerificationSubmissionCallback);
		resubmitProfileValidationButton.addClickHandler(newVerificationSubmissionCallback);

		unbindButton.addClickHandler(event -> presenter.unbindOrcId());
		certificationButton.addClickHandler(event -> presenter.goTo(new Quiz("Certification")));
		certificationPassedButton.addClickHandler(event -> DisplayUtils.newWindow("https://docs.synapse.org/articles/accounts_certified_users_and_profile_validation.html#certified-users", "_blank", ""));
		synapseTermsAcceptedButton.addClickHandler(event -> presenter.goTo(new LoginPlace(LoginPlace.SHOW_SIGNED_TOU)));
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
		clear();
	}

	@Override
	public void showPasswordChangeSuccess() {
		resetChangePasswordUI();
		showInfo("Password has been successfully changed");
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {}

	@Override
	public void updateNotificationCheckbox(UserProfile profile) {
		boolean isNotify = true;
		if (profile.getNotificationSettings() != null) {
			if (profile.getNotificationSettings().getSendEmailNotifications() != null)
				isNotify = profile.getNotificationSettings().getSendEmailNotifications();
		}
		emailNotificationsCheckbox.setValue(isNotify, false);
	}

	private ClickHandler getNotificationsClickHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// update notification settings
				presenter.updateMyNotificationSettings(emailNotificationsCheckbox.getValue(), false);
			}
		};
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public String getCurrentPasswordField() {
		return currentPasswordField.getText();
	}

	@Override
	public void setCurrentPasswordInError(boolean inError) {
		if (inError) {
			currentPassword.addStyleName("has-error");
		} else {
			currentPassword.removeStyleName("has-error");
		}
	}

	@Override
	public String getPassword1Field() {
		return password1Field.getText();
	}

	@Override
	public void setPassword1InError(boolean inError) {
		if (inError) {
			password1.addStyleName("has-error");
		} else {
			password1.removeStyleName("has-error");
		}
	}

	@Override
	public String getPassword2Field() {
		return password2Field.getText();
	}

	@Override
	public void setPassword2InError(boolean inError) {
		if (inError) {
			password2.addStyleName("has-error");
		} else {
			password2.removeStyleName("has-error");
		}
	}

	@Override
	public void clear() {
		hideAPIKey();
		resetChangePasswordUI();
		verificationApprovedButton.setVisible(false);
		resubmitProfileValidationButton.setVisible(false);
		verificationSubmittedButton.setVisible(false);
		verificationSuspendedButton.setVisible(false);
		verificationRejectedButton.setVisible(false);
		verifyAlert.setVisible(false);
	}

	@Override
	public void resetChangePasswordUI() {
		currentPasswordField.setValue("");
		password1Field.setValue("");
		password2Field.setValue("");
		changePasswordBtn.setEnabled(true);
		setCurrentPasswordInError(false);
		setPassword1InError(false);
		setPassword2InError(false);
	}

	@Override
	public void setApiKey(String apiKey) {
		apiKeyContainer.setText(apiKey);
		apiKeyUI.setVisible(true);
		changeApiKey.setVisible(true);
		showApiKey.setVisible(false);
	}


	@Override
	public void setNotificationSynAlertWidget(IsWidget notificationSynAlert) {
		notificationSynAlertPanel.setWidget(notificationSynAlert);
	}

	@Override
	public void setAPISynAlertWidget(IsWidget apiSynAlert) {
		apiSynAlertPanel.setWidget(apiSynAlert);
	}

	@Override
	public void hideAPIKey() {
		apiKeyContainer.setText("");
		apiKeyUI.setVisible(false);
		changeApiKey.setVisible(false);
		showApiKey.setVisible(true);
	}

	@Override
	public void showConfirm(String message, Callback callback) {
		DisplayUtils.confirm(message, callback);
	}

	@Override
	public void setChangePasswordEnabled(boolean isEnabled) {
		changePasswordBtn.setEnabled(isEnabled);
	}

	@Override
	public void setPasswordSynAlertWidget(IsWidget synAlert) {
		passwordSynAlertPanel.setWidget(synAlert);
	}

	@Override
	public void setSubscriptionsListWidget(Widget w) {
		subscriptionsContainer.clear();
		subscriptionsContainer.add(w);
	}

	@Override
	public void setSubscriptionsVisible(boolean visible) {
		subscriptionsContainer.setVisible(visible);
	}

	@Override
	public void setShowingUTCTime() {
		dateFormatDropdown.setText(dateFormatUTC.getText());
	}

	@Override
	public void setShowingLocalTime() {
		dateFormatDropdown.setText(dateFormatLocal.getText());
	}

	@Override
	public void setEmailAddressesWidget(IsWidget w) {
		emailsPanel.clear();
		emailsPanel.add(w);
	}


	@Override
	public void setOrcIdVisible(boolean isVisible) {
		orcIdField.setVisible(isVisible);
	}

	@Override
	public void setUnbindOrcIdVisible(boolean isVisible) {
		unbindButtonUI.setVisible(isVisible);
	}

	@Override
	public void setOrcId(String href) {
		orcIdField.setText(href);
		orcIdField.setHref(href);
	}

	@Override
	public void setOrcIDLinkButtonVisible(boolean isVisible) {
		this.linkORCIDButton.setVisible(isVisible);
	}

	@Override
	public void showNotVerified() {
		verifyAlert.setVisible(true);
	}

	@Override
	public void setResubmitVerificationButtonVisible(boolean isVisible) {
		resubmitProfileValidationButton.setVisible(isVisible);
	}

	@Override
	public void setVerificationSubmittedButtonVisible(boolean isVisible) {
		verificationSubmittedButton.setVisible(isVisible);
	}

	@Override
	public void setVerificationSuspendedButtonVisible(boolean isVisible) {
		verificationSuspendedButton.setVisible(isVisible);
	}

	@Override
	public void setVerificationRejectedButtonVisible(boolean isVisible) {
		verificationRejectedButton.setVisible(isVisible);
	}

	@Override
	public void setVerificationDetailsButtonVisible(boolean isVisible) {
		verificationApprovedButton.setVisible(isVisible);
	}

	@Override
	public void setIsCertified(boolean isCertified) {
		certificationButton.setVisible(!isCertified);
		certificationPassedButton.setVisible(isCertified);
	}
}
