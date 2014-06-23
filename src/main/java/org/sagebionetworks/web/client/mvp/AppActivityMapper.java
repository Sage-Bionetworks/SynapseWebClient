package org.sagebionetworks.web.client.mvp;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.sagebionetworks.web.client.AppLoadingView;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Challenges;
import org.sagebionetworks.web.client.place.ComingSoon;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.place.Governance;
import org.sagebionetworks.web.client.place.Help;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.ProjectsHome;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Team;
import org.sagebionetworks.web.client.place.TeamSearch;
import org.sagebionetworks.web.client.place.Quiz;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.place.WikiPlace;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.BulkPresenterProxy;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.presenter.PresenterProxy;
import org.sagebionetworks.web.client.security.AuthenticationController;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

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
		openAccessPlaces.add(LoginPlace.class);
		openAccessPlaces.add(PasswordReset.class);
		openAccessPlaces.add(RegisterAccount.class);
		openAccessPlaces.add(Synapse.class);
		openAccessPlaces.add(Wiki.class);
		openAccessPlaces.add(ProjectsHome.class);
		openAccessPlaces.add(ComingSoon.class);
		openAccessPlaces.add(Governance.class);
		openAccessPlaces.add(Challenges.class);
		openAccessPlaces.add(Help.class);
		openAccessPlaces.add(Search.class);
		openAccessPlaces.add(WikiPlace.class);
		openAccessPlaces.add(Team.class);
		openAccessPlaces.add(TeamSearch.class);
		openAccessPlaces.add(Down.class);
		openAccessPlaces.add(Profile.class);
		
		excludeFromLastPlace = new ArrayList<Class>();
		excludeFromLastPlace.add(LoginPlace.class);
		excludeFromLastPlace.add(PasswordReset.class);
		excludeFromLastPlace.add(RegisterAccount.class);
		excludeFromLastPlace.add(Quiz.class);
	}

	@Override
	public Activity getActivity(Place place) {
		synapseJSNIUtils.recordPageVisit(synapseJSNIUtils.getCurrentHistoryToken());
	    
		synapseJSNIUtils.setPageTitle(DisplayConstants.DEFAULT_PAGE_TITLE);
		synapseJSNIUtils.setPageDescription(DisplayConstants.DEFAULT_PAGE_DESCRIPTION);
	    
	    AuthenticationController authenticationController = this.ginjector.getAuthenticationController();
		GlobalApplicationState globalApplicationState = this.ginjector.getGlobalApplicationState();		
		
		// set current and last places
		Place storedCurrentPlace = globalApplicationState.getCurrentPlace(); 
		// only update move storedCurrentPlace to storedLastPlace if storedCurrentPlace is  
		if(storedCurrentPlace != null && !excludeFromLastPlace.contains(storedCurrentPlace.getClass())) {
			if (!(isFirstTime && storedCurrentPlace.getClass().equals(AppActivityMapper.getDefaultPlace().getClass())))  //if first load, then do not set the last place (if it's the default place) 
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
			} else {
				
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
			// check if this is a no-restart place change
			if(place instanceof Synapse && ((Synapse)place).isNoRestartActivity() && lastActivity != null) {
				return lastActivity;
			}
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
