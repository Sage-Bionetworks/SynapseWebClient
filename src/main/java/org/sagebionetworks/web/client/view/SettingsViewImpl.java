package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SettingsViewImpl extends Composite implements SettingsView {

	public interface SettingsViewImplUiBinder extends UiBinder<Widget, SettingsViewImpl> {}

	
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
	FlowPanel forgotPasswordContainer;
	Anchor forgotPasswordLink;
	
	@UiField
	Div emailSettingsPanel;
	@UiField
	Div changeEmailUI;
	@UiField
	FlowPanel emailsPanel;
	@UiField
	TextBox newEmailField;
	@UiField
	Button addEmailButton;

	
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
	SimplePanel addressSynAlertPanel;
	@UiField
	SimplePanel notificationSynAlertPanel;
	@UiField
	SimplePanel apiSynAlertPanel;
	@UiField
	Div subscriptionsContainer;
	@UiField
	Div passwordStrengthContainer;

	private Presenter presenter;
	
	@Inject
	public SettingsViewImpl(SettingsViewImplUiBinder binder, final SynapseJSNIUtils jsniUtils) {		
		initWidget(binder.createAndBindUi(this));
		
		ClickHandler notificationsClickHandler = getNotificationsClickHandler();
		emailNotificationsCheckbox.addClickHandler(notificationsClickHandler);
		
		changePasswordBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.changePassword();
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
		emailSettingsPanel.getElement().setAttribute(WebConstants.HIGHLIGHT_BOX_TITLE, "Email");
		changeSynapsePasswordHighlightBox.getElement().setAttribute(WebConstants.HIGHLIGHT_BOX_TITLE, "Change Synapse Password");
		apiKeyHighlightBox.getElement().setAttribute(WebConstants.HIGHLIGHT_BOX_TITLE, "Synapse API Key");
		editProfilePanel.getElement().setAttribute(WebConstants.HIGHLIGHT_BOX_TITLE, "Profile");
		
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
				presenter.addEmail(newEmailField.getValue());
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
		password1Field.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				presenter.passwordChanged(password1Field.getText());
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
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
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
		resetAddEmailUI();
	}
	
	private void resetAddEmailUI() {
		emailsPanel.clear();
		newEmailField.setValue("");
		addEmailButton.setEnabled(true);
		DisplayUtils.hide(changeEmailUI);
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
	public void showEmailChangeSuccess(String message) {
		DisplayUtils.hide(changeEmailUI);
		DisplayUtils.showInfoDialog("", message, null);
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
	public void setAddressSynAlertWidget(IsWidget addressSynAlert) {
		addressSynAlertPanel.setWidget(addressSynAlert);
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
	public void showConfirm(String message, ConfirmCallback callback) {
		Bootbox.confirm(message, callback);
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
	public void setPasswordStrengthWidget(Widget w) {
		passwordStrengthContainer.clear();
		passwordStrengthContainer.add(w);
	}
}
