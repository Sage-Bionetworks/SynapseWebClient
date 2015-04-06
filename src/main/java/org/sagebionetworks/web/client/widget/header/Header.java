package org.sagebionetworks.web.client.widget.header;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.Help;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class Header implements HeaderView.Presenter {

	public static enum MenuItems {
		DATASETS, TOOLS, NETWORKS, PEOPLE, PROJECTS
	}

	private HeaderView view;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	@Inject
	public Header(HeaderView view, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		view.setPresenter(this);
	}
	
	public void setMenuItemActive(MenuItems menuItem) {
		view.setMenuItemActive(menuItem);
	}

	public void removeMenuItemActive(MenuItems menuItem) {
		view.removeMenuItemActive(menuItem);
	}
	
	public void configure(boolean largeLogo) {
		view.setLargeLogo(largeLogo);
	}

	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
	
	public void setSearchVisible(boolean searchVisible) {
		view.setSearchVisible(searchVisible);
	}
	
	public void refresh() {
		view.refresh();
		view.setSearchVisible(true);
	}

	@Override
	public UserSessionData getUser() {
		return authenticationController.getCurrentUserSessionData(); 
	}
	@Override
	public void onGettingStartedClick() {
		globalApplicationState.getPlaceChanger().goTo(new Help(WebConstants.GETTING_STARTED));	
	}
	@Override
	public void onTrashClick() {
		globalApplicationState.getPlaceChanger().goTo(new Trash(ClientProperties.DEFAULT_PLACE_TOKEN));	
	}
	@Override
	public void onLogoutClick() {
		globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGOUT_TOKEN));	
	}
	@Override
	public void onDashboardClick() {
		if (authenticationController.isLoggedIn()) {
			globalApplicationState.getPlaceChanger().goTo(new Profile(authenticationController.getCurrentUserPrincipalId()));
		} else {
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		}	
	}
	@Override
	public void onLoginClick() {
		globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));	
	}
	@Override
	public void onRegisterClick() {
		globalApplicationState.getPlaceChanger().goTo(new RegisterAccount(ClientProperties.DEFAULT_PLACE_TOKEN));	
	}

	@Override
	public void onFavoriteClick() {
		List<EntityHeader> headers = globalApplicationState.getFavorites();
		view.clearFavorite();
		if (headers == null || headers.size() == 0) {
			view.setEmptyFavorite();
		} else {
			view.addFavorite(headers);
		}
	}
}
