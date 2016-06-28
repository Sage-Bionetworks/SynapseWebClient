package org.sagebionetworks.web.client.widget.header;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class Header implements HeaderView.Presenter, IsWidget {

	public static final String GET_SATISFACTION_SUPPORT_SITE = "http://support.sagebase.org";
	public static final String WWW_SYNAPSE_ORG = "www.synapse.org";

	public static enum MenuItems {
		DATASETS, TOOLS, NETWORKS, PEOPLE, PROJECTS
	}

	private HeaderView view;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private FavoriteWidget favWidget;
	private SynapseJSNIUtils synapseJSNIUtils;
	private StuAnnouncementWidget stuAnnouncementWidget;
	
	@Inject
	public Header(HeaderView view, AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState, SynapseClientAsync synapseClient,
			FavoriteWidget favWidget, SynapseJSNIUtils synapseJSNIUtils, StuAnnouncementWidget stuAnnouncementWidget) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.favWidget = favWidget;
		this.synapseJSNIUtils = synapseJSNIUtils;
		view.clear();
		this.stuAnnouncementWidget = stuAnnouncementWidget;
		view.setPresenter(this);
		stuAnnouncementWidget.init();
		initStagingAlert();
	}
	
	public void initStagingAlert() {
		String hostName = synapseJSNIUtils.getCurrentHostName().toLowerCase();
		boolean visible = !hostName.contains(WWW_SYNAPSE_ORG);
		view.setStagingAlertVisible(visible);
	}
	
	public void setMenuItemActive(MenuItems menuItem) {
		view.setMenuItemActive(menuItem);
	}

	public void removeMenuItemActive(MenuItems menuItem) {
		view.removeMenuItemActive(menuItem);
	}
	
	public void configure(boolean largeLogo) {
		view.setProjectHeaderText("Synapse");
		view.setProjectHeaderAnchorTarget("#");
		view.hideProjectFavoriteWidget();
		setLogo(largeLogo);
	}
	
	public void setLogo(boolean largeLogo) {
		if (largeLogo) {
			view.showLargeLogo();
		} else {
			view.showSmallLogo();
		}
	}
	
	public void configure(boolean largeLogo, EntityHeader projectHeader) {
		String projectId = projectHeader.getId();
		favWidget.configure(projectId);
		view.setProjectHeaderAnchorTarget("#!Synapse:" + projectId);
		view.setProjectHeaderText(projectHeader.getName());
		view.showProjectFavoriteWidget();
		setLogo(largeLogo);
	}

	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}

	public void setSearchVisible(boolean searchVisible) {
		view.setSearchVisible(searchVisible);
	}

	public void refresh() {
		UserSessionData userSessionData = authenticationController.getCurrentUserSessionData();
		view.setUser(userSessionData);
		view.refresh();
		view.setSearchVisible(true);
		view.setProjectFavoriteWidget(favWidget);
		view.setStuAnnouncementWidget(stuAnnouncementWidget.asWidget());
	}

	@Override
	public void onTrashClick() {
		globalApplicationState.getPlaceChanger().goTo(new Trash(ClientProperties.DEFAULT_PLACE_TOKEN));
	}

	@Override
	public void onLogoutClick() {
		//explicitly logging out, clear the current and last place.
		globalApplicationState.clearCurrentPlace();
		globalApplicationState.clearLastPlace();
		globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGOUT_TOKEN));	
	}
	
	@Override
	public void onLogoClick() {
		globalApplicationState.getPlaceChanger().goTo(new Home(ClientProperties.DEFAULT_PLACE_TOKEN));	
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
	public void onHelpForumClick() {
		view.openNewWindow(GET_SATISFACTION_SUPPORT_SITE);
	}

	@Override
	public void onFavoriteClick() {
		if(authenticationController.isLoggedIn()) {
			view.showFavoritesLoading();
			synapseClient.getFavorites(new AsyncCallback<List<EntityHeader>>() {
				@Override
				public void onSuccess(List<EntityHeader> favorites) {
					view.clearFavorite();
					globalApplicationState.setFavorites(favorites);
					if (favorites == null || favorites.size() == 0) {
						view.setEmptyFavorite();
					} else {
						view.addFavorite(favorites);
					}		
				}
				@Override
				public void onFailure(Throwable caught) {
					view.clearFavorite();
					view.setEmptyFavorite();	
				}
			});
		}
	}
}
