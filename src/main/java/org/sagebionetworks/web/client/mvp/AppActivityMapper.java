package org.sagebionetworks.web.client.mvp;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.BCCOverview;
import org.sagebionetworks.web.client.place.ComingSoon;
import org.sagebionetworks.web.client.place.Evaluation;
import org.sagebionetworks.web.client.place.Governance;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.ProjectsHome;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Settings;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.place.WikiPlace;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.BCCOverviewPresenter;
import org.sagebionetworks.web.client.presenter.ComingSoonPresenter;
import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.presenter.EvaluationPresenter;
import org.sagebionetworks.web.client.presenter.GovernancePresenter;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.presenter.PresenterProxy;
import org.sagebionetworks.web.client.presenter.ProfilePresenter;
import org.sagebionetworks.web.client.presenter.ProjectsHomePresenter;
import org.sagebionetworks.web.client.presenter.SearchPresenter;
import org.sagebionetworks.web.client.presenter.SearchUtil;
import org.sagebionetworks.web.client.presenter.SettingsPresenter;
import org.sagebionetworks.web.client.presenter.SynapseWikiPresenter;
import org.sagebionetworks.web.client.presenter.WikiPresenter;
import org.sagebionetworks.web.client.presenter.users.PasswordResetPresenter;
import org.sagebionetworks.web.client.presenter.users.RegisterAccountPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class AppActivityMapper implements ActivityMapper {
	
	private static Logger log = Logger.getLogger(AppActivityMapper.class.getName());
	private PortalGinInjector ginjector;
	@SuppressWarnings("rawtypes")
	private List<Class> openAccessPlaces; 
	private SynapseJSNIUtils synapseJSNIUtils;

	/**
	 * AppActivityMapper associates each Place with its corresponding
	 * {@link Activity}
	 * @param synapseJSNIUtilsImpl 
	 * @param clientFactory
	 *            Factory to be passed to activities
	 */
	@SuppressWarnings("rawtypes")
	public AppActivityMapper(PortalGinInjector ginjector, SynapseJSNIUtils synapseJSNIUtils) {
		super();
		this.ginjector = ginjector;
		this.synapseJSNIUtils = synapseJSNIUtils;
		
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
		openAccessPlaces.add(BCCOverview.class);
		openAccessPlaces.add(Search.class);
		openAccessPlaces.add(WikiPlace.class);
		openAccessPlaces.add(Evaluation.class);
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
		if(storedCurrentPlace != null && !(storedCurrentPlace instanceof PasswordReset) && !(storedCurrentPlace instanceof RegisterAccount)) {
			if(!(storedCurrentPlace instanceof LoginPlace) && !(place instanceof LoginPlace)) {
				// only update last place if we are not going from login to login place (this is due to SSO vs regular login difference)
				globalApplicationState.setLastPlace(storedCurrentPlace);
			}
		}
		
		globalApplicationState.setCurrentPlace(place);
				
		// If the user is not logged in then we redirect them to the login screen
		// except for the fully public places
		if(!openAccessPlaces.contains(place.getClass())) {
			if(!authenticationController.isLoggedIn()){
				// Redirect them to the login screen
				LoginPlace loginPlace = new LoginPlace(DisplayUtils.DEFAULT_PLACE_TOKEN);
				return getActivity(loginPlace);
			}			
		}
		
		// We use GIN to generate and inject all presenters with 
		// their dependencies.
		if(place instanceof Home) {
			// Split the code
			PresenterProxy<HomePresenter, Home> presenter = ginjector.getHomePresenter();
			presenter.setPlace((Home)place);
			return presenter;
		} else if(place instanceof Synapse){
			PresenterProxy<EntityPresenter, Synapse> presenter = ginjector.getEntityPresenter();
			presenter.setPlace((Synapse)place);
			return presenter;
		}else if (place instanceof ProjectsHome) {
			// Projects Home 
			PresenterProxy<ProjectsHomePresenter, ProjectsHome> presenter = ginjector.getProjectsHomePresenter();
			presenter.setPlace((ProjectsHome)place);
			return presenter;
		}else if (place instanceof LoginPlace) {
			// login view
			PresenterProxy<LoginPresenter, LoginPlace> presenter = ginjector.getLoginPresenter();
			presenter.setPlace((LoginPlace)place);
			return presenter;
		} else if (place instanceof PasswordReset) {
			// reset passwords
			PresenterProxy<PasswordResetPresenter, PasswordReset> presenter = ginjector.getPasswordResetPresenter();
			presenter.setPlace((PasswordReset)place);
			return presenter;
		} else if (place instanceof RegisterAccount) {
			// register for a new account
			PresenterProxy<RegisterAccountPresenter, RegisterAccount> presenter = ginjector.getRegisterAccountPresenter();
			presenter.setPlace((RegisterAccount)place);
			return presenter;
		} else if (place instanceof Profile) {
			// user's profile page
			PresenterProxy<ProfilePresenter, Profile> presenter = ginjector.getProfilePresenter();
			presenter.setPlace((Profile)place);
			return presenter;
		} else if (place instanceof Settings) {
			// user's profile page
			PresenterProxy<SettingsPresenter, Settings> presenter = ginjector.getSettingsPresenter();
			presenter.setPlace((Settings)place);
			return presenter;
		} else if (place instanceof ComingSoon) {
			// user's profile page
			PresenterProxy<ComingSoonPresenter, ComingSoon> presenter = ginjector.getComingSoonPresenter();
			presenter.setPlace((ComingSoon)place);
			return presenter;
		} else if (place instanceof Governance) {
			// user's profile page
			PresenterProxy<GovernancePresenter, Governance> presenter = ginjector.getGovernancePresenter();
			presenter.setPlace((Governance)place);
			return presenter;
		} else if (place instanceof BCCOverview) {
			// user's profile page
			PresenterProxy<BCCOverviewPresenter, BCCOverview> presenter = ginjector.getBCCOverviewPresenter();
			presenter.setPlace((BCCOverview)place);
			return presenter;
		} else if (place instanceof Search) {
			// search results page
			Search searchPlace = (Search) place;
			Synapse redirect = SearchUtil.willRedirect(searchPlace);
			if(redirect != null){
				return getActivity(redirect);
			}
			PresenterProxy<SearchPresenter, Search> presenter = ginjector.getSearchPresenter();
			presenter.setPlace((Search)place);
			return presenter;
		} else if (place instanceof WikiPlace) {
			// wiki page
			PresenterProxy<WikiPresenter, WikiPlace> presenter = ginjector.getWikiPresenter();
			presenter.setPlace((WikiPlace)place);
			return presenter;
		} else if (place instanceof Evaluation) {
			// evaluation
			EvaluationPresenter presenter = ginjector.getEvaluationPresenter();
			presenter.setPlace((Evaluation)place);
			return presenter;
		} else if(place instanceof Wiki){
			PresenterProxy<SynapseWikiPresenter, Wiki> presenter = ginjector.getSynapseWikiPresenter();
			presenter.setPlace((Wiki)place);
			return presenter;
		} else {
			// Log that we have an unknown place but send the user to the default
			log.log(Level.WARNING, "Unknown Place: "+place.getClass().getName());
			// Go to the default place
			return getActivity(getDefaultPlace());
		}
	}

	/**
	 * Get the default place
	 * @return
	 */
	public Place getDefaultPlace() {
		return new Home(DisplayUtils.DEFAULT_PLACE_TOKEN);
	}
	
}
