package org.sagebionetworks.web.client.widget.login;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoginWidgetViewImpl extends Composite implements
		LoginWidgetView {

	public static final String GOOGLE_OAUTH_CALLBACK_URL = "/Portal/oauth2callback?oauth2provider=GOOGLE_OAUTH_2_0";

	public interface LoginWidgetViewImplUiBinder extends UiBinder<Widget, LoginWidgetViewImpl> {}
	@UiField
	Div loginFormContainer;
	@UiField
	org.gwtbootstrap3.client.ui.Button googleSignInButton;
	@UiField
	Div synAlertContainer;
	PasswordTextBox password = null;
	TextBox username = null;
	Button submitButton = null;
	FormPanel loginForm = null;
	
	private Presenter presenter;
	
	@Inject
	public LoginWidgetViewImpl(LoginWidgetViewImplUiBinder binder) {
		initWidget(binder.createAndBindUi(this));
		loginForm = FormPanel.wrap(DOM.getElementById("login_form"));
		
		googleSignInButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(GOOGLE_OAUTH_CALLBACK_URL, "_self", "");
			}
		});
		RootPanel.detachNow(loginForm);
		username = TextBox.wrap(DOM.getElementById("synapse_username"));
	    username.getElement().setAttribute("placeholder", DisplayConstants.EMAIL_ADDRESS);
	    username.addStyleName("form-control margin-top-20 whiteBackground");
	    username.addKeyDownHandler(event -> {
	        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
	        	loginForm.submit();
	        }
		});
		username.setFocus(true);
		
		password = PasswordTextBox.wrap(DOM.getElementById("synapse_password"));
		password.getElement().setAttribute("placeholder", DisplayConstants.PASSWORD);
		password.addStyleName("form-control margin-top-20 whiteBackground");
		password.addKeyDownHandler(event -> {
	        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
	        	loginForm.submit();
	        }
		});
		
		submitButton = Button.wrap(DOM.getElementById("synapse_signin_btn"));
		submitButton.addStyleName("btn margin-top-15 btn-lg btn-success");
		submitButton.setWidth("100%");
		
		loginForm.addSubmitHandler(event -> {
			event.cancel();
			loginUser();
		});
		
		loginFormContainer.clear();
		loginFormContainer.add(loginForm);
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void clearUsername() {
		username.setValue("");	
	}
	
	@Override
	public void clear() {		
		password.setValue("");
		submitButton.setEnabled(true);
		submitButton.setText(DisplayConstants.SIGN_IN);
	}

	/*
	 * Private Methods
	 */
	private void loginUser() {
		submitButton.setEnabled(false);
		submitButton.setText(DisplayConstants.SIGNING_IN);
		presenter.setUsernameAndPassword(username.getValue(), password.getValue());
	}

	
}
