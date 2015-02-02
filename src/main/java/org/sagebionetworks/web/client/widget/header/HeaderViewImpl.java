package org.sagebionetworks.web.client.widget.header;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.security.AuthenticationControllerImpl;
import org.sagebionetworks.web.client.widget.header.Header.MenuItems;
import org.sagebionetworks.web.client.widget.search.SearchBox;

import com.google.gwt.core.client.Scheduler;
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
	Button trashLink;
	@UiField
	Button logoutLink;
	
	@UiField
	FlowPanel testSitePanel;
	@UiField
	Anchor goToStandardSite;
	@UiField
	SimplePanel searchBoxContainer;
	SimplePanel userPicturePanel;
	
	private Presenter presenter;
	private AuthenticationController authenticationController;	
	private SearchBox searchBox;	
	private SynapseJSNIUtils synapseJSNIUtils;
	private CookieProvider cookies;
	SageImageBundle sageImageBundle;
	boolean showLargeLogo;
	
	@Inject
	public HeaderViewImpl(Binder binder,
			AuthenticationControllerImpl authenticationController,
			SageImageBundle sageImageBundle,
			SearchBox searchBox,
			SynapseJSNIUtils synapseJSNIUtils, CookieProvider cookies) {
		this.initWidget(binder.createAndBindUi(this));
		this.authenticationController = authenticationController;
		this.searchBox = searchBox;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.cookies = cookies;
		this.sageImageBundle = sageImageBundle;
		// add search panel first
		searchBox.setVisible(true);
		searchBoxContainer.setWidget(searchBox.asWidget());
		userPicturePanel = new SimplePanel();
		userPicturePanel.addStyleName("displayInline margin-right-5");
		addUserPicturePanel();
		showLargeLogo = false; // default
		initClickHandlers();
		refreshTestSiteHeader();
	}
	
	/**
	 * Clear the divider/caret from the user button, and add the picture container
	 * @param button
	 */
	public void addUserPicturePanel() {
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
            	dashboardButton.add(userPicturePanel);
            }
        });
	}
	
	public void initClickHandlers() {
		goToStandardSite.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.setTestWebsite(false, cookies);
				Window.Location.reload();
			}
		});
		gettingStartedLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onGettingStartedClick();
			}
		});
		trashLink.addClickHandler(new ClickHandler() {
    		@Override
			public void onClick(ClickEvent event) {
    			presenter.onTrashClick();
			}
    	});
		
		logoutLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onLogoutClick();
			}
		});
		
		dashboardButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onDashboardClick();
			}
		});
		loginLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onLoginClick();
			}
		});
		registerLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onRegisterClick();
			}
		});
		forumLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow("http://support.sagebase.org", "", "");
			}
		});
		rLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(ClientProperties.CLIENT_R_API_URL, "", "");
			}
		});
		
		pythonLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(ClientProperties.CLIENT_PYTHON_API_URL, "", "");
			}
		});
		commandLineLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(ClientProperties.CLIENT_CL_API_URL, "", "");
			}
		});
		restAPILink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(ClientProperties.REST_API_URL, "", "");
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
	 	userPicturePanel.clear();
	 	dashboardButton.setIcon(IconType.USER);
		dashboardButton.setIconSize(IconSize.LARGE);
	 	if (userData != null && userData.getProfile() != null) {
			//has user data, update the user name and add user commands (and set to the current user name)
			UserProfile profile = userData.getProfile();
			if (profile.getPic() != null && profile.getPic().getPreviewId() != null && profile.getPic().getPreviewId().length() > 0) {
				dashboardButton.setIcon(null);
				Image profilePicture = new Image();
				profilePicture.setUrl(DisplayUtils.createUserProfileAttachmentUrl(synapseJSNIUtils.getBaseProfileAttachmentUrl(), profile.getOwnerId(), profile.getPic().getPreviewId(), null));
				profilePicture.setWidth("18px");
				profilePicture.setHeight("18px");
				profilePicture.addStyleName("userProfileImage moveup-2");
				userPicturePanel.setWidget(profilePicture);
			}
			loginLink.setVisible(false);
			registerLink.setVisible(false);
			logoutLink.setVisible(true);
		} else {
			loginLink.setVisible(true);
			registerLink.setVisible(true);
			logoutLink.setVisible(false);
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
