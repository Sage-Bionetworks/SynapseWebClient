package org.sagebionetworks.web.client.widget.header;

import java.util.Map;

import org.gwtbootstrap3.client.ui.constants.Placement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Help;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.security.AuthenticationControllerImpl;
import org.sagebionetworks.web.client.widget.header.Header.MenuItems;
import org.sagebionetworks.web.client.widget.search.SearchBox;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
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

	private static final int MAX_DISPLAY_NAME_CHARACTER_COUNT = 35;
	private static final String HEADER_LARGE_STYLE = "largeHeader";
	private static final String HEADER_SMALL_STYLE = "smallHeader";
	private static final String MARGIN_BOTTOM_STYLE = "margin-bottom-20";
	private static final String NO_TOP_MARGIN_STYLE = "notopmargin";

	private UserSessionData cachedUserSessionData = null;
	@UiField
	FlowPanel commandBar;		
	@UiField
	Image logoSmall;
	@UiField
	Image logoLarge;
	@UiField
	DivElement headerDiv;
	@UiField
	DivElement headerImageDiv;
	
	FlowPanel testSitePanel;
	
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
	private SimplePanel supportLinkContainer;
	private FlowPanel userCommands;
	private FlowPanel userNameContainer;
	private SynapseJSNIUtils synapseJSNIUtils;
	private HorizontalPanel userNameWrapper;
	private CookieProvider cookies;
	SageImageBundle sageImageBundle;
	boolean showLargeLogo;
	
	@Inject
	public HeaderViewImpl(Binder binder,
			AuthenticationControllerImpl authenticationController,
			SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle,
			GlobalApplicationState globalApplicationState, SearchBox searchBox,
			SynapseJSNIUtils synapseJSNIUtils, CookieProvider cookies) {
		this.initWidget(binder.createAndBindUi(this));
		this.iconsImageBundle = iconsImageBundle;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.searchBox = searchBox;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.cookies = cookies;
		this.sageImageBundle = sageImageBundle;
		// add search panel first
		searchBox.setVisible(true);
		
		showLargeLogo = false; // default
		
		testSitePanel = getTestPanel();
		testSitePanel.setVisible(false);
		refreshTestSiteHeader();
	}
	
	private FlowPanel getTestPanel() {
		HTMLPanel alpha = new HTMLPanel("Alpha features on&nbsp;");
		alpha.addStyleName("smallerText displayInline");

		Anchor goToStandardSite = new Anchor("off");		
		goToStandardSite.addStyleName("link smallerText displayInline");
		goToStandardSite.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.setTestWebsite(false, cookies);
				Window.Location.reload();
			}
		});
		
		FlowPanel testPanel = new FlowPanel();
		testPanel.add(alpha);
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
	}

	@Override
	public void setSearchVisible(boolean searchVisible) {
		searchBox.setVisible(searchVisible);
	}
	
	
	/*
	 * Private Methods
	 */
	
	private void setUser(UserSessionData userData) {
		commandBar.clear();
				
		//initialize buttons
		if(userAnchor == null) {
			userAnchor = new Anchor();
			userAnchor.addStyleName("headerUsernameLink");
			DisplayUtils.addTooltip(userAnchor, DisplayConstants.VIEW_DASHBOARD, Placement.BOTTOM);
			userAnchor.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					globalApplicationState.getPlaceChanger().goTo(new Profile(authenticationController.getCurrentUserPrincipalId()));
				}
			});
		}
		if (userCommands == null){
			userCommands = new FlowPanel();
        	userCommands.addStyleName("view header-inner-commands-container");
        	HTML userGuide = new HTML(DisplayUtils.getFontelloIcon("book"));
        	userGuide.addStyleName("displayInline imageButton movedown-2 margin-left-5 font-size-17");
        	DisplayUtils.addTooltip(userGuide, DisplayConstants.SYNAPSE_TUTORIAL, Placement.BOTTOM);
        	userGuide.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					globalApplicationState.getPlaceChanger().goTo(new Help(WebConstants.GETTING_STARTED));
				}
			});
    		
        	HTML trash = new HTML(DisplayUtils.getIcon("glyphicon-trash"));
        	trash.addStyleName("displayInline imageButton margin-left-5 font-size-17");
        	DisplayUtils.addTooltip(trash, DisplayConstants.TEXT_USER_TRASH, Placement.BOTTOM);
        	trash.addClickHandler(new ClickHandler() {
        		@Override
				public void onClick(ClickEvent event) {
					globalApplicationState.getPlaceChanger().goTo(new Trash(ClientProperties.DEFAULT_PLACE_TOKEN));
				}
        	});
    		
        	HTML settings = new HTML(DisplayUtils.getIcon("glyphicon-wrench"));
        	settings.addStyleName("displayInline imageButton movedown-3 margin-left-5 font-size-17");
        	DisplayUtils.addTooltip(settings, DisplayConstants.TEXT_USER_SETTINGS, Placement.BOTTOM);
        	settings.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					globalApplicationState.getPlaceChanger().goTo(new Profile(authenticationController.getCurrentUserPrincipalId(), ProfileArea.SETTINGS));
				}
			});
		 	
        	HTML logout = new HTML(DisplayUtils.getFontelloIcon("logout"));
        	logout.addStyleName("displayInline imageButton movedown-2 margin-left-5 margin-right-5 font-size-17");
        	DisplayUtils.addTooltip(logout, DisplayConstants.LABEL_LOGOUT_TEXT, Placement.BOTTOM);
        	logout.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGOUT_TOKEN));
				}
			});
        	
        	HTML dashboard = new HTML(DisplayUtils.getIcon("glyphicon-home"));
        	dashboard.addStyleName("displayInline imageButton movedown-2 margin-left-5 font-size-17");
        	DisplayUtils.addTooltip(dashboard, DisplayConstants.TEXT_USER_HOME, Placement.BOTTOM);
        	dashboard.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					globalApplicationState.getPlaceChanger().goTo(new Profile(authenticationController.getCurrentUserPrincipalId()));
				}
			});
		 	boolean isInTestWebsite = DisplayUtils.isInTestWebsite(cookies);
        	if (isInTestWebsite) 
		 		userCommands.add(dashboard);
			userCommands.add(userGuide);
			if (isInTestWebsite)
				userCommands.add(trash);
			if (!isInTestWebsite) //settings will be in the dashboard, not in the header
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
					globalApplicationState.getPlaceChanger().goTo(new RegisterAccount(ClientProperties.DEFAULT_PLACE_TOKEN));
				}
			});
		}
//		presenter.getSupportHRef(new AsyncCallback<String>() {
//			
//			@Override
//			public void onSuccess(String result) {
//				supportLink.setHref(result);
//			}
//			
//			@Override
//			public void onFailure(Throwable caught) {
//				//should never enter this code.  if the fastpass request fails, it should still return the standard support site url
//			}
//		});
		
		if (userData != null && userData.getProfile() != null) {
			//has user data, update the user name and add user commands (and set to the current user name)
			UserProfile profile = userData.getProfile();
			String displayName = DisplayUtils.getDisplayName(profile);
			if (displayName.length() > MAX_DISPLAY_NAME_CHARACTER_COUNT) { 
				displayName = displayName.substring(0, MAX_DISPLAY_NAME_CHARACTER_COUNT - 1) + "...";
			}
			userAnchor.setText(displayName);
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
				DisplayUtils.addTooltip(profilePicture, DisplayConstants.VIEW_DASHBOARD, Placement.BOTTOM);
				userNameWrapper.add(profilePicture);
			}
			userNameWrapper.add(userAnchor);
			addToCommandBar(userCommands);
			addToCommandBar(userNameContainer);
		} else {
			//no user data, add register and login
			addToCommandBar(loginButton);
			addToCommandBar(registerButton);
		}

		if (supportLink == null) {
			supportLink = new Anchor(DisplayConstants.LINK_COMMUNITY_FORUM, "http://support.sagebase.org", "_blank");
			supportLink.addStyleName("headerLink");
			supportLinkContainer = new SimplePanel();
			supportLinkContainer.add(supportLink);
		}
		addToCommandBar(supportLinkContainer);
		
		// add search	
		addToCommandBar(searchBox.asWidget());

		addToCommandBar(testSitePanel);
	}

	private void addToCommandBar(Widget widget) {
		widget.addStyleName("right vertical-align-middle inline-block margin-right-10");
		commandBar.add(widget);
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
