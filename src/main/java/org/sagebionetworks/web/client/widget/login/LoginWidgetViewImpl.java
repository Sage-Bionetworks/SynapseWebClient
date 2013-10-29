package org.sagebionetworks.web.client.widget.login;

import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.view.TermsOfUseHelper;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoginWidgetViewImpl extends Composite implements
		LoginWidgetView {

	public interface LoginWidgetViewImplUiBinder extends UiBinder<Widget, LoginWidgetViewImpl> {}
	
	@UiField
	SimplePanel googleSSOContainer;
	@UiField
	SpanElement messageLabel;
	@UiField
	Button signInBtn;
	@UiField
	Anchor forgotPasswordLink;
	@UiField
	Button registerBtn;
	@UiField
	TextBox username;
	@UiField
	PasswordTextBox password;
	
	private Presenter presenter;
	private FormData formData;
	private IconsImageBundle iconsImageBundle;
	
	@Inject
	public LoginWidgetViewImpl(LoginWidgetViewImplUiBinder binder, IconsImageBundle iconsImageBundle) {
		initWidget(binder.createAndBindUi(this));
		this.iconsImageBundle = iconsImageBundle;		
		
		signInBtn.setText(DisplayConstants.SIGN_IN);
		signInBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				loginUser();
			}
		});
		
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
		
		username.getElement().setAttribute("placeholder", DisplayConstants.EMAIL_ADDRESS);
		username.setFocus(true);
		
		password.getElement().setAttribute("placeholder", DisplayConstants.PASSWORD);
		password.addKeyDownHandler(new KeyDownHandler() {
		    @Override
		    public void onKeyDown(KeyDownEvent event) {
		        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {		        	
		        	signInBtn.fireEvent(new GwtEvent<ClickHandler>() {
		                @Override
		                public com.google.gwt.event.shared.GwtEvent.Type<ClickHandler> getAssociatedType() {
		                	return ClickEvent.getType();
		                }
		                @Override
		                protected void dispatch(ClickHandler handler) {
		                    handler.onClick(null);
		                }
		           });
		        }
		    }
		});
	}
	
	@Override 
	public void onLoad() {
		googleSSOContainer.setWidget(new HTML(createSSOLoginButton(/*acceptTermsOfUse*/explicitlyAcceptsTermsOfUse)));		
	}
	
	private SafeHtml createSSOLoginButton(boolean userHasExplictlyAcceptedTermsOfUse) {
		// federated login button
		SafeHtmlBuilder sb = new SafeHtmlBuilder()		
		.appendHtmlConstant("<form accept-charset=\"UTF-8\" action=\""+ presenter.getOpenIdActionUrl() +"\" class=\"aui\" id=\"gapp-openid-form\" method=\"post\" name=\"gapp-openid-form\">")
		.appendHtmlConstant("    <input name=\""+WebConstants.OPEN_ID_PROVIDER+"\" type=\"hidden\" value=\""+ WebConstants.OPEN_ID_PROVIDER_GOOGLE_VALUE +"\"/>")
		.appendHtmlConstant("    <input name=\""+WebConstants.RETURN_TO_URL_PARAM+"\" type=\"hidden\" value=\""+  presenter.getOpenIdReturnUrl() +"\"/>");
		if (userHasExplictlyAcceptedTermsOfUse) {
			sb.appendHtmlConstant("    <input name=\""+WebConstants.ACCEPTS_TERMS_OF_USE_PARAM+"\" type=\"hidden\" value=\"true\"/>");
		}
		sb.appendHtmlConstant("    <input name=\""+WebConstants.OPEN_ID_MODE+"\" type=\"hidden\" value=\""+  WebConstants.OPEN_ID_MODE_GWT +"\"/>");
		sb.appendHtmlConstant("    <button class=\"btn btn-default btn-lg btn-block\" id=\"" + DisplayConstants.ID_BTN_LOGIN_GOOGLE + "\" type=\"submit\"><img alt=\""+ DisplayConstants.OPEN_ID_SAGE_LOGIN_BUTTON_TEXT +" " +userHasExplictlyAcceptedTermsOfUse+" \" src=\"https://www.google.com/favicon.ico\"/>&nbsp; "+ DisplayConstants.OPEN_ID_SAGE_LOGIN_BUTTON_TEXT +"</button>")
		.appendHtmlConstant("</form>");	
		return sb.toSafeHtml();
	}
		
//	private Html ssoLoginButton;
	private boolean explicitlyAcceptsTermsOfUse = false;
		
	@Override
	public void acceptTermsOfUse() {
		explicitlyAcceptsTermsOfUse = true;
//		ssoLoginButton.setHtml(createSSOLoginButton(/*acceptTermsOfUse*/true)); // this had been creating a nullptr exception
//		vp.layout();
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
		com.google.gwt.user.client.Window.alert(message);
	}

	@Override
	public void showAuthenticationFailed() {
		messageLabel.setInnerHTML("<br/><br/><h4 class=\"text-warning\">Invalid username or password.</h4>");
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
		presenter.setUsernameAndPassword(username.getValue(), password.getValue(), false);
	}

	
}
