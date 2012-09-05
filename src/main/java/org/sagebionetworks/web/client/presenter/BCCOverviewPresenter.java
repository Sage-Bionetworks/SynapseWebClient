package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.BCCSignupAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.RssServiceAsync;
import org.sagebionetworks.web.client.place.BCCOverview;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.BCCCallback;
import org.sagebionetworks.web.client.view.BCCOverviewView;
import org.sagebionetworks.web.shared.BCCSignupProfile;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class BCCOverviewPresenter extends AbstractActivity implements BCCOverviewView.Presenter {
		
	private BCCOverview place;
	private BCCOverviewView view;
	private AuthenticationController authenticationController;
	private BCCSignupAsync bccSignup;
	private NodeModelCreator nodeModelCreator;
	private RssServiceAsync rssService;
	private GlobalApplicationState globalApplicationState;
	
	@Inject
	public BCCOverviewPresenter(BCCOverviewView view, 
			AuthenticationController authenticationController, 
			GlobalApplicationState globalApplicationState, 
			BCCSignupAsync bccSignup, 
			NodeModelCreator nodeModelCreator, 
			RssServiceAsync rssService){
		this.bccSignup=bccSignup;
		this.authenticationController = authenticationController;
		this.view = view;
		this.nodeModelCreator = nodeModelCreator;
		this.rssService = rssService;
		this.globalApplicationState = globalApplicationState;
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	public void setPlace(BCCOverview place) {
		this.place = place;
		this.view.setPresenter(this);
		this.view.showOverView();
		

		rssService.getWikiPageContent(DisplayUtils.BCC_CONTENT_PAGE_ID, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				view.showChallengeInfo(DisplayUtils.fixWikiLinks(DisplayUtils.fixEmbeddedYouTube(result)));
			}
			@Override
			public void onFailure(Throwable caught) {
				DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser());
			}
		});
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }

	@Override
	public BCCSignupProfile getBCCSignupProfile() {
		BCCSignupProfile profile = new BCCSignupProfile();
		UserSessionData userData  = authenticationController.getLoggedInUser();
		if (userData!=null) {
			fillInFromUserProfile(profile, userData.getProfile());
		}
		return profile;
	}
	
	private void fillInFromUserProfile(BCCSignupProfile bccProfile, UserProfile userProfile){
		if (userProfile.getFirstName() != null && userProfile.getFirstName().length() > 0)
			bccProfile.setFname(userProfile.getFirstName());
		if (userProfile.getLastName() != null && userProfile.getLastName().length() > 0)
			bccProfile.setLname(userProfile.getLastName());
		if (userProfile.getCompany() != null && userProfile.getCompany().length() > 0)
			bccProfile.setOrganization(userProfile.getCompany());
		if (userProfile.getPosition() != null && userProfile.getPosition().length() > 0)
			bccProfile.setTitle(userProfile.getPosition());
	}
	
	
	@Override
	public BCCCallback getBCCSignupCallback() {
		return new BCCCallback() {
			public void submit(BCCSignupProfile completedProfile) {
				bccSignup.sendSignupEmail(completedProfile, new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						view.showSubmissionError();
					}

					@Override
					public void onSuccess(Void result) {
						view.showSubmissionAcknowledgement();
					}
					
				});
			}
		};		
	}
	
}
