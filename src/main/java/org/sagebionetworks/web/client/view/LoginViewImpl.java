package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.login.AcceptTermsOfUseCallback;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.client.widget.login.UserListener;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
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
	SimplePanel logoutPanel;
	@UiField
	HTMLPanel loginView;
	
	//terms of service view
	@UiField
	HTMLPanel termsOfServiceView;
	@UiField
	DivElement termsOfServiceHighlightBox;
	@UiField
	CheckBox actEthicallyCb;
	@UiField
	CheckBox protectPrivacyCb;
	@UiField
	CheckBox noHackCb;
	@UiField
	CheckBox shareCb;
	@UiField
	CheckBox responsibilityCb;
	@UiField
	CheckBox lawsCb;
	@UiField
	Anchor viewToULink;
	@UiField
	Button takePledgeButton;
	
	private Presenter presenter;
	private LoginWidget loginWidget;
	private IconsImageBundle iconsImageBundle;
	private SageImageBundle sageImageBundle;
	private Window loggingInWindow;
	private Header headerWidget;
	private Footer footerWidget;
	public interface Binder extends UiBinder<Widget, LoginViewImpl> {}
	boolean toUInitialized;
	
	
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
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		toUInitialized = false;
		termsOfServiceHighlightBox.setAttribute(WebConstants.HIGHLIGHT_BOX_TITLE, "Awareness and Ethics Pledge");
	}

	@Override
	public void setPresenter(Presenter loginPresenter) {
		this.presenter = loginPresenter;
		header.clear();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		com.google.gwt.user.client.Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void showLoggingInLoader() {
		if(loggingInWindow == null) {
			loggingInWindow = DisplayUtils.createLoadingWindow(sageImageBundle, DisplayConstants.LABEL_SINGLE_SIGN_ON_LOGGING_IN);
		}
		loggingInWindow.show();
	}

	@Override
	public void hideLoggingInLoader() {
		loggingInWindow.hide();
	}

	@Override
	public void showLogout() {
		clear();
		headerWidget.refresh();
		
		ContentPanel cp = new ContentPanel();
		cp.setHeaderVisible(false);
		cp.setBorders(true);						
		cp.setBodyStyleName("lightGreyBackground");
		
		HTML message = new HTML();
		message.setHTML("<h4>" + DisplayConstants.LOGOUT_TEXT + "</h4>");
		cp.add(message, new MarginData(0, 0, 0, 10));
		
		com.google.gwt.user.client.ui.Button loginAgain = DisplayUtils.createButton(DisplayConstants.BUTTON_LOGIN_AGAIN, ButtonType.PRIMARY);
		loginAgain.getElement().setId(DisplayConstants.ID_BTN_LOGIN_AGAIN);
		loginAgain.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
			}
		});
		cp.add(loginAgain, new MarginData(16, 0, 10, 10));
		
		logoutPanel.add(cp);
		hideViews();
		loginView.setVisible(true);
	}

	@Override
	public void showLogin(String openIdActionUrl, String openIdReturnUrl) {
		clear();
		hideViews();
		loginView.setVisible(true);
		headerWidget.refresh();
	  	loginWidget.setOpenIdActionUrl(openIdActionUrl);
		loginWidget.setOpenIdReturnUrl(openIdReturnUrl);
		
		// Add the widget to the panel
		loginWidgetPanel.clear();
		loginWidgetPanel.add(loginWidget.asWidget());
		loginWidget.setUserListener(new UserListener() {			
			@Override
			public void userChanged(UserSessionData newUser) {
				presenter.setNewUser(newUser);
			}
		});
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
		if(loggingInWindow != null) loggingInWindow.hide();
		loginWidget.clear();
		loginWidgetPanel.clear();
		logoutPanel.clear();
	}
	
	@Override
	public void showTermsOfUse(final String content, final AcceptTermsOfUseCallback callback) {
		hideViews();
		//initialize checkboxes
		actEthicallyCb.setValue(false);
		protectPrivacyCb.setValue(false);;
		noHackCb.setValue(false);
		shareCb.setValue(false);
		responsibilityCb.setValue(false);
		lawsCb.setValue(false);

		termsOfServiceView.setVisible(true);
		//initialize if necessary
		if (!toUInitialized) {
			toUInitialized = true;
			takePledgeButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if(validatePledge()) {
						callback.accepted();
					} else {
						showErrorMessage("To take the pledge, you must first agree to all of the statements.");
					}
				}
			});
			viewToULink.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					TermsOfUseHelper.showTermsOfUse(content, null);
				}
			});
		}
     }
	
	private boolean validatePledge() {
		return actEthicallyCb.getValue() && protectPrivacyCb.getValue() && noHackCb.getValue() && shareCb.getValue() && responsibilityCb.getValue() && lawsCb.getValue();
	}
	private void hideViews() {
		loginView.setVisible(false);
		termsOfServiceView.setVisible(false);
	}
}
