package org.sagebionetworks.web.client.presenter;

import java.util.Date;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.security.AuthenticationException;
import org.sagebionetworks.web.client.view.HomeView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class HomePresenter extends AbstractActivity implements HomeView.Presenter, Presenter<Home> {
	private Home place;
	private HomeView view;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private AdapterFactory adapterFactory;
	private CookieProvider cookies;
	private ResourceLoader resourceLoader;
	private SynapseJSNIUtils jsniUtils;
	private int twitterHeight;
	public static final String TWITTER_DATA_WIDGET_ID = "624656608589561856";
	public static final String SYNAPSE_BLUE = "#1e7098";
	public static final int TWITTER_STANDARD_HEIGHT = 390;
	public static final int TWITTER_MINIMAL_HEIGHT = 200;
	public static final String TWITTER_ELEMENT_ID = "twitter-feed";
	
	@Inject
	public HomePresenter(HomeView view,  
			AuthenticationController authenticationController, 
			GlobalApplicationState globalApplicationState,
			AdapterFactory adapterFactory,
			CookieProvider cookies,
			ResourceLoader resourceLoader,
			SynapseJSNIUtils jsniUtils){
		this.view = view;
		// Set the presenter on the view
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.adapterFactory = adapterFactory;
		this.authenticationController = authenticationController;
		this.cookies = cookies;
		this.resourceLoader = resourceLoader;
		this.jsniUtils = jsniUtils;
		this.view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(Home place) {
		this.place = place;		
		view.setPresenter(this);
		checkAcceptToU();
		view.refresh();
		if(authenticationController.isLoggedIn()) {
			view.showLoggedInUI(authenticationController.getCurrentUserSessionData());
			//validate token
			validateToken();
			twitterHeight = TWITTER_MINIMAL_HEIGHT;
		} else {
			if (cookies.getCookie(CookieKeys.USER_LOGGED_IN_RECENTLY) != null) {
				view.showLoginUI();
				twitterHeight = TWITTER_STANDARD_HEIGHT;
			} else {
				view.showRegisterUI();
				twitterHeight = TWITTER_MINIMAL_HEIGHT;
			}
		}
		loadNewsFeed();
	}
		
	public void validateToken() {
		AsyncCallback<UserSessionData> callback = new AsyncCallback<UserSessionData>() {
			@Override
			public void onSuccess(UserSessionData result) {
				//do nothing
			}
			@Override
			public void onFailure(Throwable ex) {
				//token is invalid
				if (ex instanceof AuthenticationException) {
					// send user to login page						
					view.showInfo(DisplayConstants.SESSION_TIMEOUT, DisplayConstants.SESSION_HAS_TIMED_OUT);
					globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
				}
			}
		};
		UserSessionData userSessionData = authenticationController.getCurrentUserSessionData();
		if (userSessionData != null) {
			authenticationController.revalidateSession(authenticationController.getCurrentUserSessionToken(), callback);
		}
	}
	
	public void loadNewsFeed(){
		long uniqueId = new Date().getTime();
		final String twitterElementId = TWITTER_ELEMENT_ID+uniqueId;
		view.prepareTwitterContainer(twitterElementId);
	}
	
	@Override
	public void twitterContainerReady(final String elementId) {
		AsyncCallback<Void> initializedCallback = new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				jsniUtils.showTwitterFeed(TWITTER_DATA_WIDGET_ID, elementId, SYNAPSE_BLUE, SYNAPSE_BLUE, twitterHeight);
			}
			@Override
			public void onFailure(Throwable caught) {
				jsniUtils.consoleError(caught.getMessage());
			}
		};
		if (resourceLoader.isLoaded(ClientProperties.TWITTER_JS))
			jsniUtils.showTwitterFeed(TWITTER_DATA_WIDGET_ID, elementId, SYNAPSE_BLUE, SYNAPSE_BLUE, twitterHeight);
		else
			resourceLoader.requires(ClientProperties.TWITTER_JS, initializedCallback);		
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }

	
	public void checkAcceptToU() {
		if (authenticationController.isLoggedIn() && !authenticationController.getCurrentUserSessionData().getSession().getAcceptsTermsOfUse()) {
			authenticationController.logoutUser();
		}
	}
	
	@Override
	public void onUserChange() {
		globalApplicationState.getPlaceChanger().goTo(new Profile(authenticationController.getCurrentUserPrincipalId()));
	}
}
