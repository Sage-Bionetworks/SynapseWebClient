package org.sagebionetworks.web.client.widget.login;

import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.place.users.RegisterAccount;

import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoginWidgetViewImpl extends Composite implements
		LoginWidgetView {

	public interface LoginWidgetViewImplUiBinder extends UiBinder<Widget, LoginWidgetViewImpl> {}
	
	@UiField
	FlowPanel synapseLoginFieldsContainer;
	
	@UiField
	SpanElement messageLabel;
	Button signInBtn;
	Anchor forgotPasswordLink;
	@UiField
	Button registerBtn;
	@UiField
	org.gwtbootstrap3.client.ui.Button googleSignInButton;
	
	PasswordTextBox password = null;
	TextBox username = null;
	
	private Presenter presenter;
	
	@Inject
	public LoginWidgetViewImpl(LoginWidgetViewImplUiBinder binder) {
		initWidget(binder.createAndBindUi(this));
		final FormPanel form = new FormPanel();
		form.setAction("/expect_405");
		signInBtn = new SubmitButton();
		signInBtn.addStyleName("btn btn-large btn-primary");
		signInBtn.setText(DisplayConstants.SIGN_IN);
		forgotPasswordLink = new Anchor();
		forgotPasswordLink.addStyleName("link movedown-4 margin-left-10");
		forgotPasswordLink.setText(DisplayConstants.FORGOT_PASSWORD);
		forgotPasswordLink.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.goTo(new PasswordReset(ClientProperties.DEFAULT_PLACE_TOKEN));				
			}
		});
		
		registerBtn.setText(DisplayConstants.REGISTER_BUTTON);
		registerBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.goTo(new RegisterAccount(ClientProperties.DEFAULT_PLACE_TOKEN));
			}
		});
		
		googleSignInButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow("/Portal/oauth2callback?oauth2provider=GOOGLE_OAUTH_2_0", "_self", "");
			}
		});
		
		username = TextBox.wrap(DOM.getElementById("synapse_username"));
	    username.getElement().setAttribute("placeholder", DisplayConstants.EMAIL_ADDRESS);
	    username.addStyleName("form-control margin-top-20");
		username.setFocus(true);
		
		password = PasswordTextBox.wrap(DOM.getElementById("synapse_password"));
		password.getElement().setAttribute("placeholder", DisplayConstants.PASSWORD);
		password.addStyleName("form-control margin-top-20");
		password.addKeyDownHandler(new KeyDownHandler() {
		    @Override
		    public void onKeyDown(KeyDownEvent event) {
		        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
		        	form.submit();
		        }
		    }
		});
	
		FlowPanel loginFieldsPanel = new FlowPanel();
		loginFieldsPanel.add(username);
		loginFieldsPanel.add(password);
		FlowPanel buttonPanel = new FlowPanel();
		buttonPanel.addStyleName("margin-top-20");
		buttonPanel.add(signInBtn);
		buttonPanel.add(forgotPasswordLink);
		loginFieldsPanel.add(buttonPanel);
		synapseLoginFieldsContainer.add(form);
		form.setWidget(loginFieldsPanel);
		form.setMethod(FormPanel.METHOD_POST);
		form.addSubmitHandler(new FormPanel.SubmitHandler() {
			@Override
			public void onSubmit(SubmitEvent event) {
				loginUser();
			}
		});
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
	public void showError(String message) {
		messageLabel.setInnerHTML("<br/><br/><h4 class=\"text-warning\">"+message+"</h4>");
		clear();
	}

	@Override
	public void showAuthenticationFailed() {
		messageLabel.setInnerHTML("<br/><br/><h4 class=\"text-warning\">Invalid username or password.</h4> <span class=\"text-warning\">Please try again.</span>");
		clear();
	}

	@Override
	public void showTermsOfUseDownloadFailed() {
		messageLabel.setInnerHTML("<br/><br/><h4 class=\"text-warning\">Unable to Download Synapse Terms of Use.</h4>");
		clear();
	}

	@Override
	public void clearUsername() {
		username.setValue("");	
	}
	
	@Override
	public void clear() {		
		password.setValue("");
		signInBtn.setEnabled(true);
		signInBtn.setText(DisplayConstants.SIGN_IN);
	}

	/*
	 * Private Methods
	 */
	private void loginUser() {
		signInBtn.setEnabled(false);
		signInBtn.setText(DisplayConstants.SIGNING_IN);
		messageLabel.setInnerHTML(""); 
		presenter.setUsernameAndPassword(username.getValue(), password.getValue());
	}

	
}
