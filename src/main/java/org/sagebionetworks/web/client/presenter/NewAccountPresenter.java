package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.principal.AliasType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.NewAccount;
import org.sagebionetworks.web.client.view.NewAccountView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class NewAccountPresenter extends AbstractActivity implements NewAccountView.Presenter, Presenter<NewAccount> {
		
	private NewAccount place;
	private NewAccountView view;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalAppState;
	private UserAccountServiceAsync userAccountService;
	private String emailValidationToken;
	
	
	@Inject
	public NewAccountPresenter(NewAccountView view, 
			SynapseClientAsync synapseClient, 
			GlobalApplicationState globalAppState,
			UserAccountServiceAsync userAccountService){
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalAppState = globalAppState;
		this.userAccountService = userAccountService;
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(NewAccount place) {
		this.place = place;
		this.view.setPresenter(this);
		emailValidationToken = place.getFixedToken();
	}
	
	@Override
	public void completeRegistration(String userName, String fName, String lName, String password) {
		userAccountService.createUserStep2(userName, fName, lName, password, emailValidationToken, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String sessionToken) {
				//success, send to login place to continue login process (sign terms of use...)
				view.showInfo(DisplayConstants.ACCOUNT_CREATED, "");
				globalAppState.getPlaceChanger().goTo(new LoginPlace(sessionToken));
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ACCOUNT_CREATION_FAILURE + caught.getMessage());
			}
		});
	}
	

	/**
	 * Check that the username/alias is available
	 * @param username
	 */
	public void checkUsernameAvailable(String username) {
		if (username.trim().length() > 3) {
			synapseClient.isAliasAvailable(username, AliasType.USER_NAME.toString(), new AsyncCallback<Boolean>() {
				@Override
				public void onSuccess(Boolean isAvailable) {
					if (!isAvailable)
						view.markUsernameUnavailable();
				}
				
				@Override
				public void onFailure(Throwable e) {
					//do nothing.  validation has failed, but updating the username will fail if it's already taken.
					e.printStackTrace();
				}
			});
		}
	}
	
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
}
