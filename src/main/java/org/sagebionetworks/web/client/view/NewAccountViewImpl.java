package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class NewAccountViewImpl extends Composite implements NewAccountView {

	public interface NewAccountViewImplUiBinder extends UiBinder<Widget, NewAccountViewImpl> {}

	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	
	
	@UiField
	TextBox emailField;
	@UiField
	TextBox firstNameField;
	@UiField
	TextBox lastNameField;
	@UiField
	TextBox userNameField;
	@UiField
	Input password1Field;
	@UiField
	Input password2Field;
	
	@UiField
	DivElement firstName;
	@UiField
	DivElement lastName;
	@UiField
	DivElement userName;
	@UiField
	DivElement password1;
	@UiField
	DivElement password2;
	
	@UiField
	DivElement firstNameError;
	@UiField
	DivElement lastNameError;
	@UiField
	DivElement userNameError;
	@UiField
	DivElement password1Error;
	@UiField
	DivElement password2Error;
	

	@UiField
	Button registerBtn;
	
	private Presenter presenter;
	private Header headerWidget;
	private Footer footerWidget;
	
	@Inject
	public NewAccountViewImpl(NewAccountViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget,
			SageImageBundle imageBundle) {		
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		init();
	}
	
	public void init() {
		registerBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(checkUsernameFormat() && checkPassword1() && checkPassword2() && checkPasswordMatch()) {
					// formatting is ok. submit to presenter (will fail if one is taken)
					registerBtn.setEnabled(false);
					presenter.completeRegistration(userNameField.getValue(), firstNameField.getValue(), lastNameField.getValue(), password1Field.getValue());
				}
			}
		});
		
		userNameField.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				if (checkUsernameFormat())
					presenter.checkUsernameAvailable(userNameField.getValue());
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

	private boolean checkUsernameFormat() {
		DisplayUtils.hideFormError(userName, userNameError);
		if (LoginPresenter.isValidUsername(userNameField.getValue())) {
			return true;
		}
		else {
			userNameError.setInnerHTML(DisplayConstants.USERNAME_FORMAT_ERROR);
			DisplayUtils.showFormError(userName, userNameError);
			return false;
		}

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
	public void showErrorMessage(String errorMessage) {
		registerBtn.setEnabled(true);
		DisplayUtils.showErrorMessage(errorMessage);
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
		firstNameField.setValue("");
		lastNameField.setValue("");
		userNameField.setValue("");
		password1Field.setValue("");
		password2Field.setValue("");
		emailField.setValue("");
		DisplayUtils.hideFormError(userName, userNameError);
		DisplayUtils.hideFormError(password1, password1Error);
		DisplayUtils.hideFormError(password2, password2Error);
	}

	@Override
	public void markUsernameUnavailable() {
		userNameError.setInnerHTML(DisplayConstants.ERROR_USERNAME_ALREADY_EXISTS);
		DisplayUtils.showFormError(userName, userNameError);
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
	public void setEmail(String email) {
		emailField.setValue(email);
	}
}
