package org.sagebionetworks.web.client.presenter;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.sagebionetworks.web.client.AppLoadingView;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
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
import org.sagebionetworks.web.client.place.Settings;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Team;
import org.sagebionetworks.web.client.place.TeamSearch;
import org.sagebionetworks.web.client.place.Quiz;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.place.WikiPlace;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.users.PasswordResetPresenter;
import org.sagebionetworks.web.client.presenter.users.RegisterAccountPresenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

/**
 * A block of presenters in the same code split that dynamically
 * starts the correct presenter given the place
 * 
 * @author Dave
 *
 */
public class BulkPresenterProxy extends AbstractActivity {

	private static Logger log = Logger.getLogger(BulkPresenterProxy.class.getName());
	Place place;
	PortalGinInjector ginjector;
	AppLoadingView loading;
	GlobalApplicationState globalApplicationState;
	
	@Inject
	public BulkPresenterProxy(GlobalApplicationState globalApplicationState){
		this.globalApplicationState = globalApplicationState;
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		globalApplicationState.setIsEditing(false);
		GWT.runAsync(new RunAsyncCallback() {
			@Override
			public void onSuccess() {
				// detect prefetch
				if(panel == null && eventBus == null) return; 
				if(loading != null) loading.hide();
				
				 if(place instanceof Synapse){
					EntityPresenter presenter = ginjector.getEntityPresenter();
					presenter.setPlace((Synapse)place);
					presenter.start(panel, eventBus);
				}else if (place instanceof ProjectsHome) {
					// Projects Home 
					ProjectsHomePresenter presenter = ginjector.getProjectsHomePresenter();
					presenter.setPlace((ProjectsHome)place);
					presenter.start(panel, eventBus);
				}else if (place instanceof LoginPlace) {
					// login view
					LoginPresenter presenter = ginjector.getLoginPresenter();
					presenter.setPlace((LoginPlace)place);
					presenter.start(panel, eventBus);
				} else if (place instanceof PasswordReset) {
					// reset passwords
					PasswordResetPresenter presenter = ginjector.getPasswordResetPresenter();
					presenter.setPlace((PasswordReset)place);
					presenter.start(panel, eventBus);
				} else if (place instanceof RegisterAccount) {
					// register for a new account
					RegisterAccountPresenter presenter = ginjector.getRegisterAccountPresenter();
					presenter.setPlace((RegisterAccount)place);
					presenter.start(panel, eventBus);
				} else if (place instanceof Profile) {
					// user's profile page
					ProfilePresenter presenter = ginjector.getProfilePresenter();
					presenter.setPlace((Profile)place);
					presenter.start(panel, eventBus);
				} else if (place instanceof Settings) {
					// user's profile page
					SettingsPresenter presenter = ginjector.getSettingsPresenter();
					presenter.setPlace((Settings)place);
					presenter.start(panel, eventBus);
				} else if (place instanceof ComingSoon) {
					// user's profile page
					ComingSoonPresenter presenter = ginjector.getComingSoonPresenter();
					presenter.setPlace((ComingSoon)place);
					presenter.start(panel, eventBus);
				} else if (place instanceof Challenges) {
					// user's profile page
					ChallengeOverviewPresenter presenter = ginjector.getChallengeOverviewPresenter();
					presenter.setPlace((Challenges)place);
					presenter.start(panel, eventBus);
				} else if (place instanceof Help) {
					HelpPresenter presenter = ginjector.getHelpPresenter();
					presenter.setPlace((Help)place);
					presenter.start(panel, eventBus);
				} else if (place instanceof Search) {
					// search results page
					SearchPresenter presenter = ginjector.getSearchPresenter();
					presenter.setPlace((Search)place);
					presenter.start(panel, eventBus);
				} else if (place instanceof WikiPlace) {
					// wiki page
					WikiPresenter presenter = ginjector.getWikiPresenter();
					presenter.setPlace((WikiPlace)place);
					presenter.start(panel, eventBus);
				} else if(place instanceof Wiki){
					SynapseWikiPresenter presenter = ginjector.getSynapseWikiPresenter();
					presenter.setPlace((Wiki)place);
					presenter.start(panel, eventBus);
				} else if(place instanceof Down) {
					DownPresenter presenter = ginjector.getDownPresenter();
					presenter.setPlace((Down) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof Team) {
					// Team page
					TeamPresenter presenter = ginjector.getTeamPresenter();
					presenter.setPlace((Team)place);
					presenter.start(panel, eventBus);
				} else if (place instanceof TeamSearch) {
					// Team Search page
					TeamSearchPresenter presenter = ginjector.getTeamSearchPresenter();
					presenter.setPlace((TeamSearch)place);
					presenter.start(panel, eventBus);
				} else if (place instanceof Quiz) {
					// Test page
					QuizPresenter presenter = ginjector.getQuizPresenter();
					presenter.setPlace((Quiz)place);
					presenter.start(panel, eventBus);
				} else {
					// Log that we have an unknown place but send the user to the default
					log.log(Level.WARNING, "Unknown Place: "+place.getClass().getName());
					// Go to the default place
					place = getDefaultPlace();
					onSuccess();
					return;
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				// Not sure what to do here.
				DisplayUtils.showErrorMessage(caught.getMessage());
			}

		});
	}

	public void setPlace(Place place) {
		// This will get forwarded to the presenter when we get it in start()
		this.place = place;
	}

	private Place getDefaultPlace() {
		return new Home(ClientProperties.DEFAULT_PLACE_TOKEN);
	}

	public void setGinjector(PortalGinInjector ginjector) {
		this.ginjector = ginjector;
	}

	public void setloader(AppLoadingView loading) {
		this.loading = loading;		
	}
	@Override
	public String mayStop() {
		if (globalApplicationState.isEditing())
			return DisplayConstants.NAVIGATE_AWAY_CONFIRMATION_MESSAGE;
		else
			return null;
	}
	
	
}
