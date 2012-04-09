package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.BCCSignupAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.BCCOverview;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.BCCCallback;
import org.sagebionetworks.web.client.view.BCCOverviewView;
import org.sagebionetworks.web.shared.BCCSignupProfile;
import org.sagebionetworks.web.shared.users.UserData;

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
	
	@Inject
	public BCCOverviewPresenter(BCCOverviewView view, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState, BCCSignupAsync bccSignup){
		this.bccSignup=bccSignup;
		this.authenticationController = authenticationController;
		this.view = view;

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

	@Override
    public String mayStop() {
        view.clear();
        return null;
    }


	@Override
	public BCCSignupProfile getBCCSignupProfile() {
		BCCSignupProfile profile = new BCCSignupProfile();
		UserData userData = authenticationController.getLoggedInUser();
		if (userData!=null) {
			if (userData.getEmail()!=null) profile.setEmail(userData.getEmail());
			String fAndlName = userData.getUserName(); // TODO need API to get these separately
			if (null!=fAndlName) {
				int i = fAndlName.indexOf(" ");
				if (i>0) {
					profile.setFname(fAndlName.substring(0,i).trim());
					profile.setLname(fAndlName.substring(i+1).trim());
				}
			}
		}
		return profile;
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
