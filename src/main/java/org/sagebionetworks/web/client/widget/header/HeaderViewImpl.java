package org.sagebionetworks.web.client.widget.header;

import java.util.Map;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Settings;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.security.AuthenticationControllerImpl;
import org.sagebionetworks.web.client.utils.CookieProviderUtils;
import org.sagebionetworks.web.client.utils.TOOLTIP_POSITION;
import org.sagebionetworks.web.client.widget.header.Header.MenuItems;
import org.sagebionetworks.web.client.widget.search.SearchBox;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

@SuppressWarnings("unused")
public class HeaderViewImpl extends Composite implements HeaderView {

	
	public interface Binder extends UiBinder<Widget, HeaderViewImpl> {
	}

	private UserSessionData cachedUserSessionData = null;
	@UiField
	HorizontalPanel commandBar;
	
	@UiField
	SimplePanel searchBoxPanel;
	
	@UiField
	SimplePanel testSiteHeading;
	
	private Presenter presenter;
	private Map<MenuItems, Element> itemToElement;
	private AuthenticationController authenticationController;	
	private IconsImageBundle iconsImageBundle;
	private GlobalApplicationState globalApplicationState;
	private LayoutContainer jumpTo;
	private TextField<String> jumpToField;
	private Button goButton;
	private SearchBox searchBox;	
	private Anchor userAnchor;
	private Anchor loginButton;
	private Anchor registerButton;
	private Anchor supportLink;
	private HorizontalPanel userCommands;
	private FlowPanel userNameContainer;
	private SynapseJSNIUtils synapseJSNIUtils;
	private HorizontalPanel userNameWrapper;
	private CookieProvider cookies;
	
	@Inject
	public HeaderViewImpl(Binder binder, AuthenticationControllerImpl authenticationController, SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle, GlobalApplicationState globalApplicationState, SearchBox searchBox, SynapseJSNIUtils synapseJSNIUtils, CookieProvider cookies) {
		this.initWidget(binder.createAndBindUi(this));
		this.iconsImageBundle = iconsImageBundle;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.searchBox = searchBox;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.cookies = cookies;
		// add search panel
		searchBoxPanel.clear();		
		searchBoxPanel.add(searchBox.asWidget());
		searchBoxPanel.setVisible(false);
		
		testSiteHeading.clear();
		testSiteHeading.add(getTestPanel());
		testSiteHeading.setVisible(false);
		refreshTestSiteHeader();
		commandBar.addStyleName("last sf-j-menu");
	}
	
	private FlowPanel getTestPanel() {
		Anchor goToStandardSite = new Anchor("Take me back to the release version!");
		goToStandardSite.addStyleName("link");
		goToStandardSite.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				CookieProviderUtils.setTestWebsite(false, cookies);
				Window.Location.reload();
			}
		});
		FlowPanel testPanel = new FlowPanel();
		testPanel.add(new HTMLPanel("<h2>TEST VERSION</h2>"));
		testPanel.add(goToStandardSite);
		return testPanel;
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		refresh();
	}

	@Override
	public void setMenuItemActive(MenuItems menuItem) {
	}

	@Override
	public void removeMenuItemActive(MenuItems menuItem) {
	}

	private void refreshTestSiteHeader() {
		testSiteHeading.setVisible(CookieProviderUtils.isInTestWebsite(cookies));
	}
	
	@Override
	public void refresh() {
		refreshTestSiteHeader();
		UserSessionData userSessionData = presenter.getUser();
		if (cachedUserSessionData == null || !cachedUserSessionData.equals(userSessionData)){
			cachedUserSessionData = userSessionData;
			setUser(cachedUserSessionData);
		}		
	}

	@Override
	public void setSearchVisible(boolean searchVisible) {
		searchBoxPanel.setVisible(searchVisible);
	}
	
	
	/*
	 * Private Methods
	 */
	
	private void setUser(UserSessionData userData) {
		//initialize buttons
		if(userAnchor == null) {
			userAnchor = new Anchor();
			userAnchor.addStyleName("headerUsernameLink");
			userAnchor.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					globalApplicationState.getPlaceChanger().goTo(new Profile(Profile.VIEW_PROFILE_PLACE_TOKEN));
				}
			});
		}
		if (userCommands == null){
			userCommands = new HorizontalPanel();
        	userCommands.addStyleName("span-2 inner-2 view header-inner-commands-container");
   		 	Image settings = new Image(iconsImageBundle.settings16());
   		 	settings.addStyleName("imageButton");
		 	
   		 	settings.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					globalApplicationState.getPlaceChanger().goTo(new Settings(DisplayUtils.DEFAULT_PLACE_TOKEN));
				}
			});
   		 	
	 		DisplayUtils.addTooltip(this.synapseJSNIUtils, settings, DisplayConstants.TEXT_USER_SETTINGS, TOOLTIP_POSITION.BOTTOM);
		 	
   		 	Image logout = new Image(iconsImageBundle.logoutGrey16());
   		 	logout.addStyleName("imageButton");
		 	logout.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGOUT_TOKEN));
				}
			});
			DisplayUtils.addTooltip(this.synapseJSNIUtils, logout, DisplayConstants.LABEL_LOGOUT_TEXT, TOOLTIP_POSITION.BOTTOM);
		 	
		 	userCommands.add(settings);
		 	userCommands.add(logout);
		}
		
		if (userNameContainer == null){
			userNameContainer = new FlowPanel();
			userNameContainer.addStyleName("header-username-picture-container");	//border radius applies to div (FlowPanel), not table (HorizontalPanel)
			userNameWrapper = new HorizontalPanel();
			userNameContainer.add(userNameWrapper);
		}
			
		if(loginButton == null) {
			loginButton = new Anchor(DisplayConstants.BUTTON_LOGIN);
			loginButton.addStyleName("headerLink");
			loginButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
				}
			});
		}
		if (registerButton == null)
		{
			registerButton = new Anchor(DisplayConstants.BUTTON_REGISTER);
			registerButton.addStyleName("headerLink");
			registerButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					globalApplicationState.getPlaceChanger().goTo(new RegisterAccount(DisplayUtils.DEFAULT_PLACE_TOKEN));
				}
			});
		}
		if (supportLink == null) {
			supportLink = new Anchor(DisplayConstants.LINK_COMMUNITY_FORUM, "", "_blank");
			supportLink.addStyleName("headerLink");
			commandBar.add(supportLink);
		}
		presenter.getSupportHRef(new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
				supportLink.setHref(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				//should never enter this code.  if the fastpass request fails, it should still return the standard support site url
			}
		});
		
		if(userData != null) {
			//has user data, update the user name and add user commands (and set to the current user name)
			UserProfile profile = userData.getProfile();
			userAnchor.setText(profile.getDisplayName());
			commandBar.remove(loginButton);
			commandBar.remove(registerButton);
			userNameWrapper.clear();
			if (profile.getPic() != null && profile.getPic().getPreviewId() != null && profile.getPic().getPreviewId().length() > 0) {
				Image profilePicture = new Image();
				profilePicture.setUrl(DisplayUtils.createUserProfileAttachmentUrl(synapseJSNIUtils.getBaseProfileAttachmentUrl(), profile.getOwnerId(), profile.getPic().getPreviewId(), null));
				profilePicture.setWidth("20px");
				profilePicture.setHeight("20px");
				profilePicture.addStyleName("imageButton userProfileImage");
				profilePicture.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						userAnchor.fireEvent(event);
					}
				});
				userNameWrapper.add(profilePicture);
			}
			userNameWrapper.add(userAnchor);
			if (commandBar.getWidgetIndex(userNameContainer) == -1){
				commandBar.add(userNameContainer);
				commandBar.add(userCommands);
			}
		} else {
			//no user data, add register and login
			commandBar.remove(userNameContainer);
			commandBar.remove(userCommands);
			if (commandBar.getWidgetIndex(registerButton) == -1)
			{
				commandBar.add(registerButton);			
				commandBar.add(loginButton);
			}
		}
	}
	}
	
	
