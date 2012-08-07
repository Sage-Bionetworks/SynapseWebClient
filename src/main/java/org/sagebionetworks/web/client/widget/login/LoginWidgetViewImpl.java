package org.sagebionetworks.web.client.widget.login;

import org.sagebionetworks.repo.model.ServiceConstants;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.view.TermsOfUseHelper;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoginWidgetViewImpl extends LayoutContainer implements
		LoginWidgetView {

	private Presenter presenter;
	private VerticalPanel vp;
	private FormData formData;
	private Label messageLabel;
	private IconsImageBundle iconsImageBundle;
	private TextField<String> firstName = new TextField<String>();
	private TextField<String> password = new TextField<String>();
	
	@Inject
	public LoginWidgetViewImpl(IconsImageBundle iconsImageBundle) {
		this.iconsImageBundle = iconsImageBundle;
		messageLabel = new Label();
	}
	
	private SafeHtml createSSOLoginButton(boolean userHasExplictlyAcceptedTermsOfUse) {
		// federated login button
		SafeHtmlBuilder sb = new SafeHtmlBuilder()		
		.appendHtmlConstant("<form accept-charset=\"UTF-8\" action=\""+ presenter.getOpenIdActionUrl() +"\" class=\"aui\" id=\"gapp-openid-form\" method=\"post\" name=\"gapp-openid-form\">")
		.appendHtmlConstant("    <input name=\"OPEN_ID_PROVIDER\" type=\"hidden\" value=\""+ DisplayConstants.OPEN_ID_PROVIDER_GOOGLE_VALUE +"\"/>")
		.appendHtmlConstant("    <input name=\"RETURN_TO_URL\" type=\"hidden\" value=\""+  getRedirectURL() +"\"/>");
		if (userHasExplictlyAcceptedTermsOfUse) {
			sb.appendHtmlConstant("    <input name=\""+ServiceConstants.ACCEPTS_TERMS_OF_USE_PARAM+"\" type=\"hidden\" value=\"true\"/>");
		}
		sb.appendHtmlConstant("    <button id=\"" + DisplayConstants.ID_BTN_LOGIN_GOOGLE + "\" type=\"submit\"><img alt=\""+ DisplayConstants.OPEN_ID_SAGE_LOGIN_BUTTON_TEXT +" " +userHasExplictlyAcceptedTermsOfUse+" \" src=\"http://www.google.com/favicon.ico\"/>&nbsp; "+ DisplayConstants.OPEN_ID_SAGE_LOGIN_BUTTON_TEXT +"</button>")
		.appendHtmlConstant("</form>")		
		.appendHtmlConstant("<p>&nbsp;</p>");
		return sb.toSafeHtml();
	}
	
	public String getRedirectURL() {
		String redirectURL = Location.getHref();
		String tailMarker = LoginPresenter.LOGIN_PLACE;
		int i = redirectURL.indexOf(tailMarker);
		if (i>0) redirectURL = redirectURL.substring(0, i+tailMarker.length());
		return redirectURL;
	}
	
//	private Html ssoLoginButton;
	private boolean explicitlyAcceptsTermsOfUse = false;
	
	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		formData = new FormData("-20");
		vp = new VerticalPanel();
		vp.setSpacing(10);
		
		HTML ssoLoginButton = new HTML(createSSOLoginButton(/*acceptTermsOfUse*/explicitlyAcceptsTermsOfUse));
		vp.add(ssoLoginButton);
		
		createForm();
		add(vp);
	}
	
	@Override
	public void acceptTermsOfUse() {
		explicitlyAcceptsTermsOfUse = true;
//		ssoLoginButton.setHtml(createSSOLoginButton(/*acceptTermsOfUse*/true)); // this had been creating a nullptr exception
//		vp.layout();
	}

	private void createForm() {
		final FormPanel formPanel = new FormPanel();
		formPanel.setHeaderVisible(false);
		formPanel.setFrame(true);
		formPanel.setWidth(380);
		formPanel.setLabelWidth(85);
		
		// synapse login
		FieldSet fieldSet = new FieldSet();  
		fieldSet.setHeading("Login with a Synapse Account");  
		fieldSet.setCheckboxToggle(false);  		       
		FormLayout layout = new FormLayout();  
		layout.setLabelWidth(85);  		
		fieldSet.setLayout(layout);  
		fieldSet.setCollapsible(false);
		
		firstName.setFieldLabel(DisplayConstants.LOGIN_USERNAME_LABEL);
		firstName.setAllowBlank(false);
		firstName.getFocusSupport().setPreviousId(formPanel.getButtonBar().getId());
		firstName.setId(DisplayConstants.ID_INP_EMAIL_NAME);
		fieldSet.add(firstName, formData);

		password.setFieldLabel("Password");
		password.setAllowBlank(false);
		password.setPassword(true);
		password.setId(DisplayConstants.ID_INP_EMAIL_PASSWORD);
		fieldSet.add(password, formData);

		fieldSet.add(messageLabel);
		
		final Button loginButton = new Button("Login", new SelectionListener<ButtonEvent>(){			
			@Override
			public void componentSelected(ButtonEvent ce) {
				messageLabel.setText(""); 
				presenter.setUsernameAndPassword(firstName.getValue(), password.getValue(), false);
			}
		});
		loginButton.setId(DisplayConstants.ID_BTN_LOGIN2);

		fieldSet.add(loginButton);
		formPanel.add(fieldSet);		
		
		FormButtonBinding binding = new FormButtonBinding(formPanel);
		binding.addButton(loginButton);

		// Enter key submits login 
		new KeyNav<ComponentEvent>(formPanel) {
			@Override
			public void onEnter(ComponentEvent ce) {
				super.onEnter(ce);
				if(loginButton.isEnabled())
					loginButton.fireEvent(Events.Select);
			}
		};
		
		vp.add(formPanel);
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
		messageLabel.setStyleAttribute("color", "red");
		messageLabel.setText(AbstractImagePrototype.create(iconsImageBundle.warning16()).getHTML() + " Invalid username or password.");
		clear();
	}

	@Override
	public void showTermsOfUseDownloadFailed() {
		messageLabel.setStyleAttribute("color", "red");
		messageLabel.setText(AbstractImagePrototype.create(iconsImageBundle.warning16()).getHTML() + " Unable to Download Synapse Terms of Use.");
		clear();
	}

	@Override
	public void clear() {		
		password.clear();
	}

	@Override
	public void showTermsOfUse(String content, final AcceptTermsOfUseCallback callback) {
		TermsOfUseHelper.showTermsOfUse(content, callback);
     }

}
