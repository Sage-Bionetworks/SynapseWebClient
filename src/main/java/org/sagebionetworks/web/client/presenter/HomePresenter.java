package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.HomeView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class HomePresenter extends AbstractActivity implements HomeView.Presenter, Presenter<Home> {
	private Home place;
	private HomeView view;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private CookieProvider cookies;
	private ResourceLoader resourceLoader;
	private SynapseJSNIUtils jsniUtils;
	public static final String SYNAPSE_BLUE = "#1e7098";
	
	@Inject
	public HomePresenter(HomeView view,  
			AuthenticationController authenticationController, 
			GlobalApplicationState globalApplicationState,
			CookieProvider cookies,
			ResourceLoader resourceLoader,
			SynapseJSNIUtils jsniUtils){
		this.view = view;
		// Set the presenter on the view
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
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
		view.refresh();
		if(authenticationController.isLoggedIn()) {
			view.showLoggedInUI(authenticationController.getCurrentUserProfile());
			//note that the session token is validated on every place change (and on app load)
		} else {
			if (cookies.getCookie(CookieKeys.USER_LOGGED_IN_RECENTLY) != null) {
				view.showLoginUI();
			} else {
				view.showRegisterUI();
			}
		}
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
	
	@Override
	public void onUserChange() {
		globalApplicationState.getPlaceChanger().goTo(new Profile(authenticationController.getCurrentUserPrincipalId(), ProfileArea.PROJECTS));
	}
}
