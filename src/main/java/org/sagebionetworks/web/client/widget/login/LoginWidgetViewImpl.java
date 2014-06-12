package org.sagebionetworks.web.client.widget.login;

import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.view.TermsOfUseHelper;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoginWidgetViewImpl extends Composite implements
		LoginWidgetView {

	public interface LoginWidgetViewImplUiBinder extends UiBinder<Widget, LoginWidgetViewImpl> {}
	
	@UiField
	FormPanel synapseLoginFieldsContainer;
	
	@UiField
	SimplePanel googleSSOContainer;
	@UiField
	SpanElement messageLabel;
	Button signInBtn;
	Anchor forgotPasswordLink;
	@UiField
	Button registerBtn;
	
	PasswordTextBox password = null;
	TextBox username = null;
	
	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	
	@Inject
	public LoginWidgetViewImpl(LoginWidgetViewImplUiBinder binder, IconsImageBundle iconsImageBundle) {
		initWidget(binder.createAndBindUi(this));
		this.iconsImageBundle = iconsImageBundle;		
		signInBtn = new SubmitButton();
		signInBtn.addStyleName("btn btn-large btn-primary margin-top-10");
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
		
		username = TextBox.wrap(DOM.getElementById("synapse_username"));
	    username.getElement().setAttribute("placeholder", DisplayConstants.EMAIL_ADDRESS);
	    username.addStyleName("form-control");
		username.setFocus(true);
		
		password = PasswordTextBox.wrap(DOM.getElementById("synapse_password"));
		password.getElement().setAttribute("placeholder", DisplayConstants.PASSWORD);
		password.addStyleName("form-control");
		password.addKeyDownHandler(new KeyDownHandler() {
		    @Override
		    public void onKeyDown(KeyDownEvent event) {
		        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
		        	synapseLoginFieldsContainer.submit();
		        }
		    }
		});
	
		FlowPanel loginFieldsPanel = new FlowPanel();
		loginFieldsPanel.add(username);
		loginFieldsPanel.add(password);
		loginFieldsPanel.add(signInBtn);
		loginFieldsPanel.add(forgotPasswordLink);
		synapseLoginFieldsContainer.setWidget(loginFieldsPanel);
		synapseLoginFieldsContainer.addSubmitHandler(new FormPanel.SubmitHandler() {
			@Override
			public void onSubmit(SubmitEvent event) {
				loginUser();
			}
		});
	}
	
	@Override 
	public void onLoad() {
		googleSSOContainer.setWidget(new HTML(createSSOLoginButton()));		
	}
	
	private SafeHtml createSSOLoginButton() {
		// Federated login button
		SafeHtmlBuilder sb = new SafeHtmlBuilder()
				.appendHtmlConstant(
						"<form accept-charset=\"UTF-8\" action=\""
								+ presenter.getOpenIdActionUrl()
								+ "\" class=\"aui\" id=\"gapp-openid-form\" method=\"post\" name=\"gapp-openid-form\">")
				.appendHtmlConstant(
						"    <input name=\"" + WebConstants.OPEN_ID_PROVIDER
								+ "\" type=\"hidden\" value=\""
								+ WebConstants.OPEN_ID_PROVIDER_GOOGLE_VALUE
								+ "\"/>")
				.appendHtmlConstant(
						"    <input name=\"" + WebConstants.RETURN_TO_URL_PARAM
								+ "\" type=\"hidden\" value=\""
								+ presenter.getOpenIdReturnUrl() + "\"/>");
		sb.appendHtmlConstant("    <input name=\"" + WebConstants.OPEN_ID_MODE
				+ "\" type=\"hidden\" value=\"" + WebConstants.OPEN_ID_MODE_GWT
				+ "\"/>");
		sb.appendHtmlConstant(
				"    <button class=\"btn btn-default btn-lg btn-block\" id=\""
						+ DisplayConstants.ID_BTN_LOGIN_GOOGLE
						+ "\" type=\"submit\"><img alt=\""
						+ DisplayConstants.OPEN_ID_SAGE_LOGIN_BUTTON_TEXT
						+ " \" src=\"https://www.google.com/favicon.ico\"/>&nbsp; "
						+ DisplayConstants.OPEN_ID_SAGE_LOGIN_BUTTON_TEXT
						+ "</button>").appendHtmlConstant("</form>");
		return sb.toSafeHtml();
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
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showAuthenticationFailed() {
		messageLabel.setInnerHTML("<br/><br/><h4 class=\"text-warning\">Invalid username or password.</h4> <span class=\"text-warning\">If you are confident that the credentials filled in are correct, please report this issue to synapseInfo@sagebase.org</span>");
		clear();
	}

	@Override
	public void showTermsOfUseDownloadFailed() {
		messageLabel.setInnerHTML("<br/><br/><h4 class=\"text-warning\">Unable to Download Synapse Terms of Use.</h4>");
		clear();
	}

	@Override
	public void clear() {		
		password.setValue("");
		username.setValue("");
		signInBtn.setEnabled(true);
		signInBtn.setText(DisplayConstants.SIGN_IN);
	}

	@Override
	public void showTermsOfUse(String content, final AcceptTermsOfUseCallback callback) {
		TermsOfUseHelper.showTermsOfUse(content, callback);
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
