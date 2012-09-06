package org.sagebionetworks.web.client.presenter;

import java.util.Iterator;

import org.sagebionetworks.repo.model.RSSEntry;
import org.sagebionetworks.repo.model.RSSFeed;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.BCCSignupAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.RssServiceAsync;
import org.sagebionetworks.web.client.place.BCCOverview;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.BCCCallback;
import org.sagebionetworks.web.client.view.BCCOverviewView;
import org.sagebionetworks.web.shared.BCCSignupProfile;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

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
	}
	

	public String getHtml(String rssFeedJson) throws RestServiceException {
		RSSFeed feed = nodeModelCreator.createEntity(rssFeedJson, RSSFeed.class);
		StringBuilder htmlResponse = new StringBuilder();
		for (Iterator iterator = feed.getEntries().iterator(); iterator.hasNext();) {
			RSSEntry entry = (RSSEntry) iterator.next();
			htmlResponse.append("<h1><a href=\"" + entry.getLink() + "\">" + entry.getTitle() + "</a></h1>\n");
			htmlResponse.append("<p class=\"clear small-italic notopmargin nobottommargin\">" + entry.getDate() + "</p>\n");
			htmlResponse.append(entry.getContent());
		}
		return htmlResponse.toString();
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
