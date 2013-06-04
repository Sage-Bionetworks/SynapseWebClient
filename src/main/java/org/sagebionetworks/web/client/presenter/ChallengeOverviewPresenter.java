package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.BCCSignupAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.RssServiceAsync;
import org.sagebionetworks.web.client.place.Challenges;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.ChallengeOverviewView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class ChallengeOverviewPresenter extends AbstractActivity implements ChallengeOverviewView.Presenter, Presenter<Challenges> {
		
	private Challenges place;
	private ChallengeOverviewView view;
	private AuthenticationController authenticationController;
	private BCCSignupAsync bccSignup;
	private NodeModelCreator nodeModelCreator;
	private RssServiceAsync rssService;
	private GlobalApplicationState globalApplicationState;
	
	@Inject
	public ChallengeOverviewPresenter(ChallengeOverviewView view, 
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

	@Override
	public void setPlace(Challenges place) {
		this.place = place;
		this.view.setPresenter(this);
		this.view.showOverView();
		

		rssService.getCachedContent(DisplayUtils.CHALLENGE_OVERVIEW_CONTENT_PROVIDER_ID, new AsyncCallback<String>() {
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
}
