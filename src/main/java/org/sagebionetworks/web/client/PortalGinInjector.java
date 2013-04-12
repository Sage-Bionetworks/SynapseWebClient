package org.sagebionetworks.web.client;

import org.sagebionetworks.web.client.place.BCCOverview;
import org.sagebionetworks.web.client.place.ComingSoon;
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
import org.sagebionetworks.web.client.presenter.GovernancePresenter;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.presenter.PresenterProxy;
import org.sagebionetworks.web.client.presenter.ProfilePresenter;
import org.sagebionetworks.web.client.presenter.ProjectsHomePresenter;
import org.sagebionetworks.web.client.presenter.SearchPresenter;
import org.sagebionetworks.web.client.presenter.SettingsPresenter;
import org.sagebionetworks.web.client.presenter.SynapseWikiPresenter;
import org.sagebionetworks.web.client.presenter.WikiPresenter;
import org.sagebionetworks.web.client.presenter.users.PasswordResetPresenter;
import org.sagebionetworks.web.client.presenter.users.RegisterAccountPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

/**
 * The root portal dependency injection root.
 * 
 * @author jmhill
 *
 */
@GinModules(PortalGinModule.class)
public interface PortalGinInjector extends Ginjector {

	public GlobalApplicationState getGlobalApplicationState();
	
	public PresenterProxy<HomePresenter, Home> getHomePresenter();

	public PresenterProxy<EntityPresenter, Synapse> getEntityPresenter();
	
	public PresenterProxy<ProjectsHomePresenter, ProjectsHome> getProjectsHomePresenter();
	
	public PresenterProxy<LoginPresenter, LoginPlace> getLoginPresenter();
	
	public AuthenticationController getAuthenticationController();
	
	public PresenterProxy<PasswordResetPresenter, PasswordReset> getPasswordResetPresenter();
	
	public PresenterProxy<RegisterAccountPresenter, RegisterAccount> getRegisterAccountPresenter();

	public PresenterProxy<ProfilePresenter, Profile> getProfilePresenter();

	public PresenterProxy<SettingsPresenter, Settings> getSettingsPresenter();
	
	public PresenterProxy<ComingSoonPresenter, ComingSoon> getComingSoonPresenter();
	
	public PresenterProxy<BCCOverviewPresenter, BCCOverview> getBCCOverviewPresenter();
	
	public PresenterProxy<GovernancePresenter, Governance> getGovernancePresenter();
	
	public PresenterProxy<SearchPresenter, Search> getSearchPresenter();
	
	public PresenterProxy<SynapseWikiPresenter, Wiki> getSynapseWikiPresenter();
	
	public PresenterProxy<WikiPresenter, WikiPlace> getWikiPresenter();
	
	public EventBus getEventBus();
		
}
