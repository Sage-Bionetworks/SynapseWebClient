package org.sagebionetworks.web.client.presenter;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.sagebionetworks.web.client.AppLoadingView;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.Portal;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Account;
import org.sagebionetworks.web.client.place.Certificate;
import org.sagebionetworks.web.client.place.Challenges;
import org.sagebionetworks.web.client.place.ChangeUsername;
import org.sagebionetworks.web.client.place.ComingSoon;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.place.ErrorPlace;
import org.sagebionetworks.web.client.place.Help;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.NewAccount;
import org.sagebionetworks.web.client.place.PeopleSearch;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.ProjectsHome;
import org.sagebionetworks.web.client.place.Quiz;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.SignedToken;
import org.sagebionetworks.web.client.place.StandaloneWiki;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Team;
import org.sagebionetworks.web.client.place.TeamSearch;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.users.PasswordResetPresenter;
import org.sagebionetworks.web.client.presenter.users.RegisterAccountPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.footer.VersionState;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

/**
 * A block of presenters in the same code split that dynamically starts the
 * correct presenter given the place
 * 
 * @author Dave
 * 
 */
public class BulkPresenterProxy extends AbstractActivity {

	private static Logger log = Logger.getLogger(BulkPresenterProxy.class
			.getName());
	Place place;
	PortalGinInjector ginjector;
	AppLoadingView loading;
	GlobalApplicationState globalApplicationState;
	GWTWrapper gwt;
	SynapseJSNIUtils jsniUtils;
	AsyncCallback<VersionState> versionCheckCallback;
	@Inject
	public BulkPresenterProxy(GlobalApplicationState globalApplicationState,
			GWTWrapper gwt,
			SynapseJSNIUtils jsniUtils) {
		this.globalApplicationState = globalApplicationState;
		this.gwt = gwt;
		this.jsniUtils = jsniUtils;
		versionCheckCallback = new AsyncCallback<VersionState>() {
			@Override
			public void onFailure(Throwable caught) {
				//do nothing
			}
			@Override
			public void onSuccess(VersionState result) {
				if (result.isVersionChange()) {
					//Going to a new place but the version is not up to date.
					//Update the app version first.
					Window.Location.reload();
				}
			}
		};
	}
	
	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		globalApplicationState.checkVersionCompatibility(versionCheckCallback);
		globalApplicationState.setIsEditing(false);
		GWT.runAsync(new RunAsyncCallback() {
			@Override
			public void onSuccess() {
				// detect prefetch
				if (panel == null && eventBus == null) return;
				if (loading != null) loading.hide();
				if (place instanceof Synapse) {
					EntityPresenter presenter = ginjector.getEntityPresenter();
					presenter.setPlace((Synapse) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof ProjectsHome) {
					// Projects Home
					ProjectsHomePresenter presenter = ginjector.getProjectsHomePresenter();
					presenter.setPlace((ProjectsHome) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof LoginPlace) {
					// login view
					LoginPresenter presenter = ginjector.getLoginPresenter();
					presenter.setPlace((LoginPlace) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof PasswordReset) {
					// reset passwords
					PasswordResetPresenter presenter = ginjector.getPasswordResetPresenter();
					presenter.setPlace((PasswordReset) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof RegisterAccount) {
					// register for a new account
					RegisterAccountPresenter presenter = ginjector.getRegisterAccountPresenter();
					presenter.setPlace((RegisterAccount) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof Profile) {
					// user's profile page
					ProfilePresenter presenter = ginjector.getProfilePresenter();
					presenter.setPlace((Profile) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof ComingSoon) {
					// user's profile page
					ComingSoonPresenter presenter = ginjector.getComingSoonPresenter();
					presenter.setPlace((ComingSoon) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof Challenges) {
					// user's profile page
					ChallengeOverviewPresenter presenter = ginjector.getChallengeOverviewPresenter();
					presenter.setPlace((Challenges) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof Help) {
					HelpPresenter presenter = ginjector.getHelpPresenter();
					presenter.setPlace((Help) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof Search) {
					// search results page
					SearchPresenter presenter = ginjector.getSearchPresenter();
					presenter.setPlace((Search) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof Wiki) {
					SynapseWikiPresenter presenter = ginjector.getSynapseWikiPresenter();
					presenter.setPlace((Wiki) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof Down) {
					DownPresenter presenter = ginjector.getDownPresenter();
					presenter.setPlace((Down) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof Team) {
					// Team page
					TeamPresenter presenter = ginjector.getTeamPresenter();
					presenter.setPlace((Team) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof TeamSearch) {
					// Team Search page
					TeamSearchPresenter presenter = ginjector.getTeamSearchPresenter();
					presenter.setPlace((TeamSearch) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof PeopleSearch) {
					// People Search Page
					PeopleSearchPresenter presenter = ginjector.getPeopleSearchPresenter();
					presenter.setPlace((PeopleSearch) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof Quiz) {
					// Test page
					QuizPresenter presenter = ginjector.getQuizPresenter();
					presenter.setPlace((Quiz) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof Certificate) {
					CertificatePresenter presenter = ginjector.getCertificatePresenter();
					presenter.setPlace((Certificate) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof Account) {
					AccountPresenter presenter = ginjector.getAccountPresenter();
					presenter.setPlace((Account) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof ChangeUsername) {
					ChangeUsernamePresenter presenter = ginjector.getChangeUsernamePresenter();
					presenter.setPlace((ChangeUsername) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof Trash) {
					TrashPresenter presenter = ginjector.getTrashPresenter();
					presenter.setPlace((Trash) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof NewAccount) {
					NewAccountPresenter presenter = ginjector.getNewAccountPresenter();
					presenter.setPlace((NewAccount) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof StandaloneWiki) {
					SynapseStandaloneWikiPresenter presenter = ginjector.getSynapseStandaloneWikiPresenter();
					presenter.setPlace((StandaloneWiki) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof SignedToken) {
					SignedTokenPresenter presenter = ginjector.getSignedTokenPresenter();
					presenter.setPlace((SignedToken) place);
					presenter.start(panel, eventBus);
				} else if (place instanceof ErrorPlace) {
					ErrorPresenter presenter = ginjector.getErrorPresenter();
					presenter.setPlace((ErrorPlace) place);
					presenter.start(panel, eventBus);
				} else {
					// Log that we have an unknown place but send the user to the default
					log.log(Level.WARNING, "Unknown Place: " + place.getClass().getName());
					// Go to the default place
					place = getDefaultPlace();
					onSuccess();
					return;
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				//SWC-2444: if there is a problem getting the code, try to reload the app
				jsniUtils.consoleError(caught.getMessage());
				gwt.scheduleExecution(new Callback() {
					@Override
					public void invoke() {
						Window.Location.reload();		
					}
				}, Portal.CODE_LOAD_DELAY);
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
