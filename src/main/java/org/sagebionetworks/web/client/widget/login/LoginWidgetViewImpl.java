package org.sagebionetworks.web.client.widget.login;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SageImageBundle;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoginWidgetViewImpl implements LoginWidgetView, IsWidget {

	// SWC-4943: This was to disable Last Pass (due to leftover LP UI left in the SPA DOM after login). But functionality of the pw manager is more important! 
	//public static final String LASTPASS_IGNORE_INPUT_FIELD = "data-lpignore";
	public static final String GOOGLE_OAUTH_CALLBACK_URL = "/Portal/oauth2callback?oauth2provider=GOOGLE_OAUTH_2_0";
	public static final String GOOGLE_OAUTH_WITH_STATE_CALLBACK_URL = GOOGLE_OAUTH_CALLBACK_URL + "&state=";
	
	public interface LoginWidgetViewImplUiBinder extends UiBinder<Widget, LoginWidgetViewImpl> {}
	@UiField
	Button loginButton;
	@UiField
	TextBox username;
	@UiField
	Input password;
	@UiField
	Button googleSignInButton;
	@UiField
	Div synAlertContainer;
	private Presenter presenter;
	Widget widget;
	
	@Inject
	public LoginWidgetViewImpl(LoginWidgetViewImplUiBinder binder, SageImageBundle sageImageBundle) {
		widget = binder.createAndBindUi(this);
		
		Image googleLogo = new Image(sageImageBundle.logoGoogle());
		googleLogo.addStyleName("whiteBackground left padding-10 rounded");
		googleLogo.setHeight("42px");
		googleLogo.setWidth("42px");
		googleSignInButton.add(googleLogo);
		Span googleText = new Span("Sign in with Google");
		googleText.addStyleName("movedown-9");
		googleSignInButton.add(googleText);
		googleSignInButton.addClickHandler(event -> Window.Location.assign(GOOGLE_OAUTH_CALLBACK_URL));
		
		loginButton.addClickHandler(event -> {
			loginUser();
		});
		username.addKeyDownHandler(event -> {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
				loginUser();
			}
		});
		username.setFocus(true);
//		username.getElement().setAttribute(LASTPASS_IGNORE_INPUT_FIELD, "true");
		password.addKeyDownHandler(event -> {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
				loginUser();
			}
		});
//		password.getElement().setAttribute(LASTPASS_IGNORE_INPUT_FIELD, "true");
	}
	
	@Override
	public Widget asWidget() {
		return widget;
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
		loginButton.setEnabled(true);
		loginButton.setText(DisplayConstants.SIGN_IN);
	}

	private void loginUser() {
		loginButton.setEnabled(false);
		loginButton.setText(DisplayConstants.SIGNING_IN);
		presenter.setUsernameAndPassword(username.getValue(), password.getValue());
	}
}
