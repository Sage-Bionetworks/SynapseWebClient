package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.login.AcceptTermsOfUseCallback;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.client.widget.login.UserListener;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
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

	private Presenter presenter;
	private LoginWidget loginWidget;
	private IconsImageBundle iconsImageBundle;
	private SageImageBundle sageImageBundle;
	private Window logginInWindow;
	private Header headerWidget;
	private Footer footerWidget;
	private Dialog window;
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
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());

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
		headerWidget.refresh();
		
		ContentPanel cp = new ContentPanel();
		cp.setHeaderVisible(false);
		cp.setBorders(true);						
		cp.setBodyStyleName("lightGreyBackground");
		
		HTML message = new HTML();
		if(isSsoLogout) {
			message.setHTML("<h4>"				
					+ DisplayConstants.LOGOUT_TEXT
					+ "</h4><br/><br/>"
					+ DisplayUtils.getIconHtml(iconsImageBundle.warning16())
					+ " " + DisplayConstants.LOGOUT_SSO_TEXT);
		} else {
			message.setHTML("<h4>" + DisplayConstants.LOGOUT_TEXT + "</h4>");
		}
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
		logoutPanel.clear();
	}
	
	@Override
	public void showTermsOfUse(String content, final AcceptTermsOfUseCallback callback) {
        TermsOfUseHelper.showTermsOfUse(content, callback);
     }
	
	@Override
	public void showSetUsernameDialog(final CallbackP<String> callback) {
        if (window == null) {
        	window = new Dialog();
	        window.setMaximizable(false);
//	        window.setWidth(400);
//	        window.setHeight(500);
	        window.setPlain(true); 
	        window.setModal(true); 
	        window.setHeading("Username"); 
	        window.setLayout(new FlowLayout());
	        window.setScrollMode(Scroll.AUTO);
	        window.setButtons(Dialog.OK);
	        window.setHideOnButtonClick(false);
	        
	        final TextBox username = new TextBox();  
		     
	        LayoutContainer lc = new LayoutContainer();
	        lc.add(createForm(username));
	        Button saveButton = window.getButtonById(Dialog.OK);
//	        saveButton.setStyleName("btn btn-primary");
	        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
	            @Override
	            public void componentSelected(ButtonEvent ce) {
	            	if (username.getValue() != null && username.getValue().trim().length() > 0 && callback!=null)
	            		callback.invoke(username.getValue().trim());
	            	else
	            		showErrorMessage(DisplayConstants.FILL_IN_USERNAME);
	            }
	        });
	        window.add(lc);
        }
        // show the window
        window.show();		
	}
	
	@Override
	public void hideSetUsernameDialog() {
		if (window != null)
			window.hide();
	}

	 private FormPanel createForm(TextBox username) {
		 FormPanel formPanel = new FormPanel();
		 formPanel.getElement().setAttribute("role", "form");
		 
		 FlowPanel textFieldPanel = new FlowPanel();
		 textFieldPanel.setStyleName("form-group margin-10");
		 HTML infoPanel = new HTML(DisplayConstants.FILL_IN_USERNAME);
		 infoPanel.setStyleName("bs-callout bs-callout-info");
	    
	     textFieldPanel.add(infoPanel);
	     username.getElement().setAttribute("placeholder", "Your new username");
	     username.setStyleName("form-control");
	     username.getElement().setId(DisplayConstants.ID_INP_USERNAME);
	     textFieldPanel.add(username);
	     formPanel.add(textFieldPanel);
	     return formPanel;
	 }
	
}
