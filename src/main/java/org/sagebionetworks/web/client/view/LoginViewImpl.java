package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.login.AcceptTermsOfUseCallback;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.client.widget.login.UserListener;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoginViewImpl extends Composite implements LoginView {
	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	SimplePanel loginWidgetPanel;
	@UiField
	SimplePanel passwordResetButtonPanel;
	@UiField 
	SimplePanel registerButtonPanel;
	@UiField
	SimplePanel logoutPanel;

	private Presenter presenter;
	private LoginWidget loginWidget;
	private IconsImageBundle iconsImageBundle;
	private SageImageBundle sageImageBundle;
	private Window logginInWindow;
	private Header headerWidget;
	private Footer footerWidget;
	
	public interface Binder extends UiBinder<Widget, LoginViewImpl> {}
	
	@Inject
	public LoginViewImpl(Binder uiBinder, IconsImageBundle icons,
			Header headerWidget, Footer footerWidget,
			SageImageBundle sageImageBundle, LoginWidget loginWidget) {
		initWidget(uiBinder.createAndBindUi(this));
		this.loginWidget = loginWidget;
		this.iconsImageBundle = icons;
		this.sageImageBundle = sageImageBundle;
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());

	}

	@Override
	public void setPresenter(Presenter loginPresenter) {
		this.presenter = loginPresenter;
		header.clear();
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		com.google.gwt.user.client.Window.scrollTo(0, 0); // scroll user to top of page
	}
	
	@Override
	public void showLoggingInLoader() {
		if(logginInWindow == null) {
			logginInWindow = DisplayUtils.createLoadingWindow(sageImageBundle, DisplayConstants.LABEL_SINGLE_SIGN_ON_LOGGING_IN);
		}
		logginInWindow.show();
	}

	@Override
	public void hideLoggingInLoader() {
		logginInWindow.hide();
	}

	@Override
	public void showLogout(boolean isSsoLogout) {
		clear();
		
		ContentPanel cp = new ContentPanel();
		cp.setWidth(385);
		cp.setHeaderVisible(false);
		cp.setBorders(true);						
		cp.setBodyStyleName("lightGreyBackground");
		cp.add(new HTML("<h2>"+ DisplayConstants.LABEL_LOGOUT_TEXT +"</h2>"), new MarginData(0, 0, 0, 10));
		
		HTML message = new HTML();
		if(isSsoLogout) {
			message.setHTML(DisplayUtils.getIconHtml(iconsImageBundle
					.informationBalloon16())
					+ " "
					+ DisplayConstants.LOGOUT_TEXT
					+ "<br/><br/>"
					+ DisplayUtils.getIconHtml(iconsImageBundle.warning16())
					+ " " + DisplayConstants.LOGOUT_SSO_TEXT);
		} else {
			message.setHTML(DisplayUtils.getIconHtml(iconsImageBundle.informationBalloon16()) + " " + DisplayConstants.LOGOUT_TEXT);
		}
		cp.add(message, new MarginData(0, 0, 0, 10));
		
		Button loginAgain = new Button(DisplayConstants.BUTTON_LOGIN_AGAIN);
		loginAgain.setId(DisplayConstants.ID_BTN_LOGIN_AGAIN);
		loginAgain.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				presenter.goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
			}
		});
		cp.add(loginAgain, new MarginData(16, 0, 10, 10));
		
		logoutPanel.add(cp);
	}

	@Override
	public void showLogin(String openIdActionUrl, String openIdReturnUrl) {
		clear();
		headerWidget.refresh();
		loginWidget.setOpenIdActionUrl(openIdActionUrl);
		loginWidget.setOpenIdReturnUrl(openIdReturnUrl);
		
		// Add the widget to the panel
		loginWidgetPanel.clear();
		loginWidgetPanel.add(loginWidget.asWidget());
		loginWidget.addUserListener(new UserListener() {
			
			@Override
			public void userChanged(UserSessionData newUser) {
				presenter.setNewUser(newUser);
			}
		});
				
		Button forgotPasswordButton = new Button("Forgot Password?", AbstractImagePrototype.create(iconsImageBundle.help16()), new SelectionListener<ButtonEvent>() {			
			@Override
			public void componentSelected(ButtonEvent ce) {
				presenter.goTo(new PasswordReset(DisplayUtils.DEFAULT_PLACE_TOKEN));								
			}
		});
		forgotPasswordButton.setId(DisplayConstants.ID_BTN_FORGOT_PWD);
		passwordResetButtonPanel.clear();
		passwordResetButtonPanel.add(forgotPasswordButton);
		

		Button registerButton = new Button("Register for a Synapse Account", AbstractImagePrototype.create(iconsImageBundle.userBusiness16()), new SelectionListener<ButtonEvent>() {			
			@Override
			public void componentSelected(ButtonEvent ce) {
				presenter.goTo(new RegisterAccount(DisplayUtils.DEFAULT_PLACE_TOKEN));
			}
		});
		registerButton.setId(DisplayConstants.ID_BTN_REGISTER2);
		registerButtonPanel.add(registerButton);
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
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
		if(logginInWindow != null) logginInWindow.hide();
		loginWidget.clear();
		loginWidgetPanel.clear();
		passwordResetButtonPanel.clear();
		registerButtonPanel.clear();
		logoutPanel.clear();
	}
	
	@Override
	public void showTermsOfUse(String content, final AcceptTermsOfUseCallback callback) {
        TermsOfUseHelper.showTermsOfUse(content, callback);
     }

	@Override
	public void acceptTermsOfUse() {
		loginWidget.acceptTermsOfUse();
	}


}
