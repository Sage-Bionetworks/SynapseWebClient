package org.sagebionetworks.web.client.view.users;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.widget.filter.QueryFilter;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RegisterAccountViewImpl extends Composite implements RegisterAccountView {

	public interface RegisterAccountViewImplUiBinder extends UiBinder<Widget, RegisterAccountViewImpl> {}
	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;	

	
	@UiField
	TextBox firstNameField;
	@UiField
	TextBox lastNameField;
	@UiField
	TextBox userNameField;
	@UiField
	TextBox emailAddressField;
	
	@UiField
	DivElement emailAddress;
	@UiField
	DivElement firstName;
	@UiField
	DivElement lastName;
	@UiField
	DivElement userName;
	
	@UiField
	DivElement registrationForm;
	
	@UiField
	DivElement emailAddressError;
	@UiField
	DivElement firstNameError;
	@UiField
	DivElement lastNameError;
	@UiField
	DivElement userNameError;

	
	@UiField
	Button registerBtn;
	
	@UiField
	DivElement contentHtml;
	

	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private Header headerWidget;
	private Footer footerWidget;
	
	@Inject
	public RegisterAccountViewImpl(RegisterAccountViewImplUiBinder binder, Header headerWidget, Footer footerWidget, IconsImageBundle iconsImageBundle, QueryFilter filter) {		
		initWidget(binder.createAndBindUi(this));

		this.iconsImageBundle = iconsImageBundle;
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		init();
	}

	public void init() {
		registerBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (checkUsernameFormat() && checkEmailFormat()) {
					// formatting is ok. submit to presenter (will fail if one is taken)
					registerBtn.setEnabled(false);
					presenter.registerUser(userNameField.getValue(),
							emailAddressField.getValue(),
							firstNameField.getValue(), 
							lastNameField.getValue());
				}

			}
		});
		emailAddressField.getElement().setAttribute("placeholder", "Enter email");
		userNameField.getElement().setAttribute("placeholder", "Enter username");
		firstNameField.getElement().setAttribute("placeholder", "Enter first name (optional)");
		lastNameField.getElement().setAttribute("placeholder", "Enter last name (optional)");
		userNameField.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				if (checkUsernameFormat())
					presenter.checkUsernameAvailable(userNameField.getValue());
			}
		});
		
		emailAddressField.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				if (checkEmailFormat())
					presenter.checkEmailAvailable(emailAddressField.getValue());
			}
		});
	}
	
	private boolean checkEmailFormat(){
		DisplayUtils.hideFormError(emailAddress, emailAddressError);
		if (LoginPresenter.isValidEmail(emailAddressField.getValue())) {
			return true;
		}
		else {
			emailAddressError.setInnerHTML(WebConstants.INVALID_EMAIL_MESSAGE);
			DisplayUtils.showFormError(emailAddress, emailAddressError);
			return false;
		}
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
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		headerWidget.configure(false);
		headerWidget.refresh();
	}

	@Override
	public void showDefault() {
		this.clear();
		DisplayUtils.show(registrationForm);
		registerBtn.setVisible(true);
	}


	@Override
	public void showAccountCreated() {
		clear();
		String loginPlaceHref = DisplayUtils.getLoginPlaceHistoryToken(LoginPlace.LOGIN_TOKEN);
		contentHtml.setInnerHTML(DisplayUtils.getInfoHtml(DisplayConstants.ACCOUNT_CREATED + "<a class=\"link\" href=\""+loginPlaceHref+"\">"+DisplayConstants.LOGIN_HERE+"</a>."));				
	}

	@Override
	public void showErrorMessage(String errorMessage) {
		registerBtn.setEnabled(true);
		DisplayUtils.showErrorMessage(errorMessage);
	}

	@Override
	public void clear() {		
		if(contentHtml != null) contentHtml.setInnerHTML("");
		DisplayUtils.hide(registrationForm);
		registerBtn.setVisible(false);
		registerBtn.setEnabled(true);
		firstNameField.setValue("");
		lastNameField.setValue("");
		userNameField.setValue("");
		emailAddressField.setValue("");
		DisplayUtils.hideFormError(emailAddress, emailAddressError);
		DisplayUtils.hideFormError(userName, userNameError);
		
		header.clear();
		footer.clear();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
	}

	@Override
	public void markUsernameUnavailable() {
		userNameError.setInnerHTML(DisplayConstants.ERROR_USERNAME_ALREADY_EXISTS);
		DisplayUtils.showFormError(userName, userNameError);
	}
	
	@Override
	public void markEmailUnavailable() {
		emailAddressError.setInnerHTML(DisplayConstants.ERROR_EMAIL_ALREADY_EXISTS);
		DisplayUtils.showFormError(emailAddress, emailAddressError);
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showLoading() {
	}
	
}
