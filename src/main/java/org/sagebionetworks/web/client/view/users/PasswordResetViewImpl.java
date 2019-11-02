package org.sagebionetworks.web.client.view.users;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.InfoAlert;
import org.sagebionetworks.web.client.widget.header.Header;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PasswordResetViewImpl extends Composite implements PasswordResetView {

	public interface PasswordResetViewImplUiBinder extends UiBinder<Widget, PasswordResetViewImpl> {
	}

	@UiField
	DivElement resetPasswordForm;
	@UiField
	DivElement sendPasswordChangeForm;
	@UiField
	InfoAlert passwordResetRequired;
	@UiField
	PasswordTextBox currentPasswordField;
	@UiField
	PasswordTextBox password1Field;
	@UiField
	PasswordTextBox password2Field;
	@UiField
	TextBox emailAddressField;

	@UiField
	DivElement currentPassword;
	@UiField
	DivElement password1;
	@UiField
	DivElement password2;
	@UiField
	Div emailAddress;

	@UiField
	DivElement currentPasswordError;
	@UiField
	DivElement password1Error;
	@UiField
	DivElement password2Error;
	@UiField
	Alert emailAddressError;

	@UiField
	Heading pageTitle;
	@UiField
	SimplePanel loadingPanel;

	@UiField
	Button submitBtn;

	@UiField
	SpanElement contentHtml;

	@UiField
	Div synAlertContainer;

	private Presenter presenter;
	private Header headerWidget;

	private boolean isShowingResetUI;

	@Inject
	public PasswordResetViewImpl(PasswordResetViewImplUiBinder binder, Header headerWidget) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;

		headerWidget.configure();
		init();

		loadingPanel.setVisible(false);
		showRequestForm();
	}

	private boolean checkEmail() {
		emailAddressError.setVisible(false);
		if (DisplayUtils.isDefined(emailAddressField.getValue())) {
			return true;
		} else {
			emailAddressError.setText(DisplayConstants.ERROR_ALL_FIELDS_REQUIRED);
			emailAddressError.setVisible(true);
			return false;
		}
	}

	private boolean checkCurrentPassword() {
		DisplayUtils.hideFormError(currentPassword, currentPasswordError);
		if (!DisplayUtils.isDefined(currentPasswordField.getText())) {
			currentPasswordError.setInnerHTML(DisplayConstants.ERROR_ALL_FIELDS_REQUIRED);
			DisplayUtils.showFormError(currentPassword, currentPasswordError);
			return false;
		} else
			return true;
	}

	private boolean checkPassword1() {
		DisplayUtils.hideFormError(password1, password1Error);
		if (!DisplayUtils.isDefined(password1Field.getText())) {
			password1Error.setInnerHTML(DisplayConstants.ERROR_ALL_FIELDS_REQUIRED);
			DisplayUtils.showFormError(password1, password1Error);
			return false;
		} else
			return true;
	}

	private boolean checkPassword2() {
		DisplayUtils.hideFormError(password2, password2Error);
		if (!DisplayUtils.isDefined(password2Field.getText())) {
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

	public void init() {
		isShowingResetUI = false;
		KeyDownHandler submit = new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					submitBtn.click();
				}
			}
		};
		password1Field.addKeyDownHandler(submit);
		password2Field.addKeyDownHandler(submit);
		emailAddressField.addKeyDownHandler(submit);
		submitBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (isShowingResetUI) {
					// validate passwords are filled in and match
					if (checkCurrentPassword() && checkPassword1() && checkPassword2() && checkPasswordMatch()) {
						submitBtn.setEnabled(false);
						presenter.resetPassword(currentPasswordField.getValue(), password1Field.getValue());
					}
				} else {
					// validate email address is filled in
					if (checkEmail()) {
						submitBtn.setEnabled(false);
						presenter.requestPasswordReset(emailAddressField.getValue());
					}
				}
			}
		});

		emailAddressField.getElement().setAttribute("placeholder", "Email address -or- username");
		password1Field.getElement().setAttribute("placeholder", "Enter password");
		password2Field.getElement().setAttribute("placeholder", "Confirm password");

		emailAddressField.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				checkEmail();
			}
		});
		password1Field.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				checkPassword1();
			}
		});

		password2Field.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				checkPassword2();
			}
		});
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		headerWidget.refresh();
	}

	@Override
	public void showRequestForm() {
		clear();
		pageTitle.setText(DisplayConstants.SEND_PASSWORD_CHANGE_REQUEST);
		submitBtn.setText("Reset my password");
		DisplayUtils.show(sendPasswordChangeForm);
		isShowingResetUI = false;
		submitBtn.setVisible(true);
	}

	@Override
	public void showResetForm() {
		clear();
		pageTitle.setText(DisplayConstants.SET_PASSWORD);
		submitBtn.setText("Set my password");
		DisplayUtils.show(resetPasswordForm);
		isShowingResetUI = true;
		submitBtn.setVisible(true);
	}

	@Override
	public void clear() {
		if (contentHtml != null)
			contentHtml.setInnerHTML("");
		loadingPanel.setVisible(false);
		submitBtn.setText("Submit");
		DisplayUtils.hide(sendPasswordChangeForm);
		DisplayUtils.hide(resetPasswordForm);
		submitBtn.setEnabled(true);
		submitBtn.setVisible(false);
		password1Field.setValue("");
		password2Field.setValue("");
		emailAddressField.setValue("");
		emailAddressError.setVisible(false);
		DisplayUtils.hideFormError(password1, password1Error);
		DisplayUtils.hideFormError(password2, password2Error);
		headerWidget.configure();
		passwordResetRequired.setVisible(false);
	}

	@Override
	public void showPasswordResetSuccess() {
		clear();
		pageTitle.setText(DisplayConstants.SUCCESS);
		contentHtml.setInnerHTML(DisplayUtils.getInfoHtml(DisplayConstants.PASSWORD_HAS_BEEN_CHANGED));
	}

	@Override
	public void showErrorMessage(String errorMessage) {
		submitBtn.setEnabled(true);
		DisplayUtils.showErrorMessage(errorMessage);
	}


	@Override
	public void showRequestSentSuccess() {
		clear();
		pageTitle.setText(DisplayConstants.REQUEST_SENT);
		contentHtml.setInnerHTML(DisplayUtils.getInfoHtml(DisplayConstants.PASSWORD_RESET_SENT));
	}

	@Override
	public void showLoading() {
		loadingPanel.setWidget(DisplayUtils.getSmallLoadingWidget());
		loadingPanel.setVisible(true);
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showExpiredRequest() {
		loadingPanel.setVisible(false);
		pageTitle.setText(DisplayConstants.REQUEST_EXPIRED);
		contentHtml.setInnerHTML(DisplayConstants.SET_PASSWORD_EXPIRED);
	}

	public void setSynAlertWidget(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	};

	@Override
	public void setSubmitButtonEnabled(boolean enabled) {
		submitBtn.setEnabled(enabled);
	}

	@Override
	public void showPasswordResetRequired() {
		passwordResetRequired.setVisible(true);
	}
}
