package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtilsImpl;
import org.sagebionetworks.web.client.ValidationUtils;
import org.sagebionetworks.web.client.place.ChangeUsername;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.ChangeUsernameView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class ChangeUsernamePresenter extends AbstractActivity implements ChangeUsernameView.Presenter, Presenter<ChangeUsername> {
		
	private ChangeUsername place;
	private ChangeUsernameView view;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalAppState;
	private AuthenticationController authController;
	private JSONObjectAdapter jsonObjectAdapter;
	private SynapseAlert synAlert;
	
	@Inject
	public ChangeUsernamePresenter(ChangeUsernameView view, 
			SynapseClientAsync synapseClient, 
			GlobalApplicationState globalAppState,
			AuthenticationController authController,
			JSONObjectAdapter jsonObjectAdapter,
			SynapseAlert synAlert){
		this.view = view;
		fixServiceEntryPoint(synapseClient);
		this.synapseClient = synapseClient;
		this.globalAppState = globalAppState;
		this.authController = authController;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.synAlert = synAlert;
		view.setPresenter(this);
		view.setSynapseAlertWidget(synAlert.asWidget());
	}
	
	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(ChangeUsername place) {
		this.place = place;
		this.view.setPresenter(this);
	}
	
	@Override
	public void setUsername(String newUsername) {
		synAlert.clear();
		UserProfile profile = authController.getCurrentUserProfile();
		if (profile != null) {
			//quick check to see if it's valid.
			if (ValidationUtils.isValidUsername(newUsername)) {
				profile.setUserName(newUsername);
				
				AsyncCallback<Void> profileUpdatedCallback = new AsyncCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						view.showInfo("Successfully updated your username");
						globalAppState.gotoLastPlace();
					}
					
					@Override
					public void onFailure(Throwable caught) {
						synAlert.handleException(caught);
					}
				};
				updateProfile(profile, profileUpdatedCallback);
			} else {
				//invalid username
				synAlert.showError("Username format is invalid. " + DisplayConstants.USERNAME_FORMAT_ERROR);
				view.clear();
			}
		}
	}
	
	public void updateProfile(final UserProfile newProfile, final AsyncCallback<Void> callback) {
		synapseClient.updateUserProfile(newProfile, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				authController.updateCachedProfile(newProfile);
				callback.onSuccess(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	@Override
    public String mayStop() {
		synAlert.clear();
        view.clear();
        return null;
    }	
	
}
