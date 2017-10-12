package org.sagebionetworks.web.client.mvp;


import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import org.sagebionetworks.web.client.*;
import org.sagebionetworks.web.client.place.*;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.BulkPresenterProxy;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.presenter.PresenterProxy;
import org.sagebionetworks.web.client.security.AuthenticationController;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AppActivityMapper implements ActivityMapper {	

	private static Logger log = Logger.getLogger(AppActivityMapper.class.getName());
	private PortalGinInjector ginjector;
	@SuppressWarnings("rawtypes")
	private List<Class> openAccessPlaces; 
	private List<Class> excludeFromLastPlace;
	private SynapseJSNIUtils synapseJSNIUtils;
	AppLoadingView loading;
	Activity lastActivity;
	private static boolean isFirstTime = false;

	/**
	 * AppActivityMapper associates each Place with its corresponding
	 * {@link Activity}
	 * @param synapseJSNIUtilsImpl 
	 * @param clientFactory
	 *            Factory to be passed to activities
	 */
	@SuppressWarnings("rawtypes")
	public AppActivityMapper(PortalGinInjector ginjector, SynapseJSNIUtils synapseJSNIUtils, AppLoadingView loading) {
		super();
		this.ginjector = ginjector;
		this.synapseJSNIUtils = synapseJSNIUtils; 
		this.loading = loading;
		
		openAccessPlaces = new ArrayList<Class>();
		openAccessPlaces.add(Home.class);
		openAccessPlaces.add(ErrorPlace.class);
		openAccessPlaces.add(LoginPlace.class);
		openAccessPlaces.add(PasswordReset.class);
		openAccessPlaces.add(RegisterAccount.class);
		openAccessPlaces.add(NewAccount.class);
		openAccessPlaces.add(Synapse.class);
		openAccessPlaces.add(Wiki.class);
		openAccessPlaces.add(ProjectsHome.class);
		openAccessPlaces.add(ComingSoon.class);
		openAccessPlaces.add(Governance.class);
		openAccessPlaces.add(Challenges.class);
		openAccessPlaces.add(Help.class);
		openAccessPlaces.add(Search.class);
		openAccessPlaces.add(Team.class);
		openAccessPlaces.add(MapPlace.class);
		openAccessPlaces.add(TeamSearch.class);
		openAccessPlaces.add(PeopleSearch.class);
		openAccessPlaces.add(Down.class);
		openAccessPlaces.add(Profile.class);
		openAccessPlaces.add(Certificate.class);
		openAccessPlaces.add(StandaloneWiki.class);
		openAccessPlaces.add(SignedToken.class);
		openAccessPlaces.add(SynapseForumPlace.class);
		openAccessPlaces.add(EmailInvitation.class);

		excludeFromLastPlace = new ArrayList<Class>();
		excludeFromLastPlace.add(Home.class);
		excludeFromLastPlace.add(ErrorPlace.class);
		excludeFromLastPlace.add(LoginPlace.class);
		excludeFromLastPlace.add(PasswordReset.class);
		excludeFromLastPlace.add(RegisterAccount.class);
		excludeFromLastPlace.add(NewAccount.class);
		excludeFromLastPlace.add(Quiz.class);
		excludeFromLastPlace.add(ChangeUsername.class);
		excludeFromLastPlace.add(Trash.class);
		excludeFromLastPlace.add(Certificate.class);
		excludeFromLastPlace.add(SignedToken.class);
		excludeFromLastPlace.add(Down.class);
	}

	@Override
	public Activity getActivity(Place place) {
		synapseJSNIUtils.setPageTitle(DisplayConstants.DEFAULT_PAGE_TITLE);
		synapseJSNIUtils.setPageDescription(DisplayConstants.DEFAULT_PAGE_DESCRIPTION);
	    
	    AuthenticationController authenticationController = this.ginjector.getAuthenticationController();
		GlobalApplicationState globalApplicationState = this.ginjector.getGlobalApplicationState();
		
		globalApplicationState.recordPlaceVisit(place);
		
		// set current and last places
		Place storedCurrentPlace = globalApplicationState.getCurrentPlace(); 
		// only update move storedCurrentPlace to storedLastPlace if storedCurrentPlace is  
		if(storedCurrentPlace != null && !excludeFromLastPlace.contains(storedCurrentPlace.getClass())) {
			if (!(isFirstTime && storedCurrentPlace.getClass().equals(Profile.class)))  //if first load, then do not set the last place (if it's a default place) 
				globalApplicationState.setLastPlace(storedCurrentPlace);			
		}
		
		isFirstTime = false;
		
		globalApplicationState.setCurrentPlace(place);
		
		// If the user is not logged in then we redirect them to the login screen
		// except for the fully public places
		if(!openAccessPlaces.contains(place.getClass())) {
			if(!authenticationController.isLoggedIn()){
				// Redirect them to the login screen
				LoginPlace loginPlace = new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN);
				return getActivity(loginPlace);
			}
		}
		
		// We use GIN to generate and inject all presenters with 
		// their dependencies.
		if(place instanceof Home) {
			// Split the code
			PresenterProxy<HomePresenter, Home> presenter = ginjector.getHomePresenter();
			presenter.setPlace((Home)place);
			presenter.setGinInjector(ginjector);
			lastActivity = presenter;
			return presenter;
		} else {
			if(loading != null) loading.showWidget();
			BulkPresenterProxy bulkPresenterProxy = ginjector.getBulkPresenterProxy();
			bulkPresenterProxy.setGinjector(ginjector);
			bulkPresenterProxy.setloader(loading);
			bulkPresenterProxy.setPlace(place);
			lastActivity = bulkPresenterProxy;
			return bulkPresenterProxy;
		}
	}

	/**
	 * Get the default place
	 * @return
	 */
	public static Place getDefaultPlace() {
		return new Home(ClientProperties.DEFAULT_PLACE_TOKEN);
	}
	
}
