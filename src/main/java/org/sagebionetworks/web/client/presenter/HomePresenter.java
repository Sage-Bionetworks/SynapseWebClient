package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.HomeView;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class HomePresenter extends AbstractActivity implements Presenter<Home> {
	private Home place;
	private HomeView view;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private CookieProvider cookies;
	public static final String SYNAPSE_BLUE = "#1e7098";

	@Inject
	public HomePresenter(HomeView view, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState, CookieProvider cookies) {
		this.view = view;
		// Set the presenter on the view
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.cookies = cookies;
		view.scrollToTop();
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(Home place) {
		this.place = place;
		view.refresh();
		if (authenticationController.isLoggedIn()) {
			view.showLoggedInUI(authenticationController.getCurrentUserProfile());
			// note that the session token is validated on every place change (and on app load)
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
}
