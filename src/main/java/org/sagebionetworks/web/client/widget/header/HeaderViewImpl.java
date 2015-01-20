package org.sagebionetworks.web.client.widget.header;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Help;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.security.AuthenticationControllerImpl;
import org.sagebionetworks.web.client.widget.header.Header.MenuItems;
import org.sagebionetworks.web.client.widget.search.SearchBox;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HeaderViewImpl extends Composite implements HeaderView {
	public interface Binder extends UiBinder<Widget, HeaderViewImpl> {
	}

	private static final int MAX_DISPLAY_NAME_CHARACTER_COUNT = 35;
	private static final String HEADER_LARGE_STYLE = "largeHeader";
	private static final String HEADER_SMALL_STYLE = "smallHeader";

	private UserSessionData cachedUserSessionData = null;
	@UiField
	Image logoSmall;
	@UiField
	Image logoLarge;
	@UiField
	DivElement headerDiv;
	@UiField
	DivElement headerImageDiv;
	
	@UiField
	Button dashboardButton;
	
	@UiField
	AnchorListItem gettingStartedLink;
	@UiField
	AnchorListItem forumLink;
	@UiField
	AnchorListItem rLink;
	@UiField
	AnchorListItem pythonLink;
	@UiField
	AnchorListItem commandLineLink;
	@UiField
	AnchorListItem restAPILink;
	
	@UiField
	Anchor registerLink;
	@UiField
	Anchor loginLink;
	
	@UiField
	Button userButton;
	@UiField
	AnchorListItem usernameLink;
	@UiField
	AnchorListItem settingsLink;
	@UiField
	AnchorListItem trashLink;
	@UiField
	AnchorListItem logoutLink;
	
	@UiField
	FlowPanel testSitePanel;
	@UiField
	Anchor goToStandardSite;
	@UiField
	SimplePanel searchBoxContainer;
	
	private Presenter presenter;
	private AuthenticationController authenticationController;	
	private GlobalApplicationState globalApplicationState;
	private SearchBox searchBox;	
	private SynapseJSNIUtils synapseJSNIUtils;
	private CookieProvider cookies;
	SageImageBundle sageImageBundle;
	boolean showLargeLogo;
	
	@Inject
	public HeaderViewImpl(Binder binder,
			AuthenticationControllerImpl authenticationController,
			SageImageBundle sageImageBundle,
			GlobalApplicationState globalApplicationState, SearchBox searchBox,
			SynapseJSNIUtils synapseJSNIUtils, CookieProvider cookies) {
		this.initWidget(binder.createAndBindUi(this));
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.searchBox = searchBox;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.cookies = cookies;
		this.sageImageBundle = sageImageBundle;
		// add search panel first
		searchBox.setVisible(true);
		
		searchBoxContainer.setWidget(searchBox.asWidget());
		showLargeLogo = false; // default
		initClickHandlers();
		refreshTestSiteHeader();
	}
	
	public void initClickHandlers() {
		goToStandardSite.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.setTestWebsite(false, cookies);
				Window.Location.reload();
			}
		});
		usernameLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new Profile(authenticationController.getCurrentUserPrincipalId()));
			}
		});
		gettingStartedLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new Help(WebConstants.GETTING_STARTED));
			}
		});
		trashLink.addClickHandler(new ClickHandler() {
    		@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new Trash(ClientProperties.DEFAULT_PLACE_TOKEN));
			}
    	});
		settingsLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new Profile(authenticationController.getCurrentUserPrincipalId(), ProfileArea.SETTINGS));
			}
		});
		logoutLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGOUT_TOKEN));
			}
		});
		
		dashboardButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (authenticationController.isLoggedIn()) {
					globalApplicationState.getPlaceChanger().goTo(new Profile(authenticationController.getCurrentUserPrincipalId()));
				} else {
					globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
				}
			}
		});
		loginLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
			}
		});
		registerLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new RegisterAccount(ClientProperties.DEFAULT_PLACE_TOKEN));
			}
		});
		forumLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow("http://support.sagebase.org", "", "");
			}
		});

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
		testSitePanel.setVisible(DisplayUtils.isInTestWebsite(cookies));
	}
	
	@Override
	public void refresh() {
		setLogo();
		refreshTestSiteHeader();
		UserSessionData userSessionData = presenter.getUser();
		if (cachedUserSessionData == null || !cachedUserSessionData.equals(userSessionData)){
			cachedUserSessionData = userSessionData;
			setUser(cachedUserSessionData);
		}
		boolean isInTestWebsite = DisplayUtils.isInTestWebsite(cookies);
	 	trashLink.setVisible(isInTestWebsite);
	}

	@Override
	public void setSearchVisible(boolean searchVisible) {
		searchBox.setVisible(searchVisible);
	}
	
	
	/*
	 * Private Methods
	 */
	
	private void setUser(UserSessionData userData) {
		boolean isInTestWebsite = DisplayUtils.isInTestWebsite(cookies);
	 	trashLink.setVisible(isInTestWebsite);
	 		
		
		if (userData != null && userData.getProfile() != null) {
			//has user data, update the user name and add user commands (and set to the current user name)
			UserProfile profile = userData.getProfile();
			String displayName = DisplayUtils.getDisplayName(profile);
			if (displayName.length() > MAX_DISPLAY_NAME_CHARACTER_COUNT) { 
				displayName = displayName.substring(0, MAX_DISPLAY_NAME_CHARACTER_COUNT - 1) + "...";
			}
			usernameLink.setText(displayName);
			if (profile.getPic() != null && profile.getPic().getPreviewId() != null && profile.getPic().getPreviewId().length() > 0) {
				Image profilePicture = new Image();
				profilePicture.setUrl(DisplayUtils.createUserProfileAttachmentUrl(synapseJSNIUtils.getBaseProfileAttachmentUrl(), profile.getOwnerId(), profile.getPic().getPreviewId(), null));
				profilePicture.setWidth("20px");
				profilePicture.setHeight("20px");
				profilePicture.addStyleName("userProfileImage");
				userButton.clear();
				userButton.add(profilePicture);
			}
			userButton.setVisible(true);
			loginLink.setVisible(false);
			registerLink.setVisible(false);
		} else {
			userButton.setVisible(false);
			loginLink.setVisible(true);
			registerLink.setVisible(true);
		}
	}
	
	@Override
	public void setLargeLogo(boolean isLarge) {
		this.showLargeLogo = isLarge;
	}
	
	private void setLogo() {
		if(showLargeLogo) {
			logoLarge.setVisible(true);
			logoSmall.setVisible(false);
			headerDiv.removeClassName(HEADER_SMALL_STYLE);
			headerDiv.addClassName(HEADER_LARGE_STYLE);
		} else {						
			logoLarge.setVisible(false);
			logoSmall.setVisible(true);
			headerDiv.removeClassName(HEADER_LARGE_STYLE);
			headerDiv.addClassName(HEADER_SMALL_STYLE);
		}
	}
	
}
