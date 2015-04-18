package org.sagebionetworks.web.client.view.users;

import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RegisterAccountViewImpl extends Composite implements RegisterAccountView {

	public interface RegisterAccountViewImplUiBinder extends UiBinder<Widget, RegisterAccountViewImpl> {}
	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;	
	@UiField
	TextBox emailAddressField;
	@UiField
	DivElement emailAddress;
	@UiField
	DivElement registrationForm;
	@UiField
	DivElement emailAddressError;
	@UiField
	Button registerBtn;
	@UiField
	DivElement contentHtml;
	
	private Presenter presenter;
	private Header headerWidget;
	private Footer footerWidget;
	
	@Inject
	public RegisterAccountViewImpl(RegisterAccountViewImplUiBinder binder, Header headerWidget, Footer footerWidget) {		
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		init();
	}

	public void init() {
		registerBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (checkEmailFormat()) {
					// formatting is ok. submit to presenter (will fail if one is taken)
					presenter.registerUser(emailAddressField.getValue());
				}

			}
		});
		
		emailAddressField.addKeyDownHandler(new KeyDownHandler() {
		    @Override
		    public void onKeyDown(KeyDownEvent event) {
		        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
		        	registerBtn.click();
		        }
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
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		headerWidget.configure();
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
		contentHtml.setInnerHTML(DisplayUtils.getInfoHtml(DisplayConstants.ACCOUNT_EMAIL_SENT));				
	}

	@Override
	public void showErrorMessage(String errorMessage) {
		DisplayUtils.showErrorMessage(errorMessage);
	}
	
	@Override
	public void enableRegisterButton(boolean enable) {
		registerBtn.setEnabled(enable);
	}
	
	@Override
	public void clear() {		
		if(contentHtml != null) contentHtml.setInnerHTML("");
		DisplayUtils.hide(registrationForm);
		registerBtn.setVisible(false);
		registerBtn.setEnabled(true);
		emailAddressField.setValue("");
		DisplayUtils.hideFormError(emailAddress, emailAddressError);
		
		header.clear();
		footer.clear();
		headerWidget.configure();
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
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

	@Override
	public void setEmail(String email) {
		emailAddressField.setText(email);
	}
	
}
