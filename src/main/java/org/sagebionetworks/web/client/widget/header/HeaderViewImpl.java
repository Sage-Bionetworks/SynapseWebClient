package org.sagebionetworks.web.client.widget.header;

import java.util.Map;
import java.util.TreeMap;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Settings;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.security.AuthenticationControllerImpl;
import org.sagebionetworks.web.client.widget.header.Header.MenuItems;
import org.sagebionetworks.web.client.widget.search.SearchBox;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

@SuppressWarnings("unused")
public class HeaderViewImpl extends Composite implements HeaderView {

	private String baseProfileAttachmentUrl = GWT.getModuleBaseURL()+"profileAttachment";
	
	public interface Binder extends UiBinder<Widget, HeaderViewImpl> {
	}

	private UserSessionData cachedUserSessionData = null;
	@UiField
	HorizontalPanel commandBar;
	
	@UiField
	SimplePanel searchBoxPanel;
		
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
	
	@Inject
	public HeaderViewImpl(Binder binder, AuthenticationControllerImpl authenticationController, SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle, GlobalApplicationState globalApplicationState, SearchBox searchBox, SynapseJSNIUtils synapseJSNIUtils) {
		this.initWidget(binder.createAndBindUi(this));
		this.iconsImageBundle = iconsImageBundle;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.searchBox = searchBox;
		this.synapseJSNIUtils = synapseJSNIUtils;
		// add search panel
		searchBoxPanel.clear();		
		searchBoxPanel.add(searchBox.asWidget());
		searchBoxPanel.setVisible(false);
		commandBar.addStyleName("last sf-j-menu");
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

	@Override
	public void refresh() {
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
   		 	
	 		Map<String, String> optionsMap = new TreeMap<String, String>();
			optionsMap.put("title", DisplayConstants.TEXT_USER_SETTINGS);
			optionsMap.put("data-placement", "bottom");
			optionsMap.put("data-animation", "false");
			DisplayUtils.addTooltip(this.synapseJSNIUtils, settings, optionsMap);
		 	
   		 	Image logout = new Image(iconsImageBundle.logoutGrey16());
   		 	logout.addStyleName("imageButton");
		 	logout.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGOUT_TOKEN));
				}
			});
		 	optionsMap = new TreeMap<String, String>();
			optionsMap.put("title", DisplayConstants.LABEL_LOGOUT_TEXT);
			optionsMap.put("data-placement", "bottom");
			optionsMap.put("data-animation", "false");
			DisplayUtils.addTooltip(this.synapseJSNIUtils, logout, optionsMap);
		 	
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
				profilePicture.setUrl(DisplayUtils.createUserProfileAttachmentUrl(baseProfileAttachmentUrl, profile.getOwnerId(), profile.getPic().getPreviewId(), null));
				profilePicture.setWidth("20px");
				profilePicture.setHeight("20px");
				profilePicture.addStyleName("margin:auto; display:block;");
				profilePicture.addStyleName("imageButton");
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
	
	
