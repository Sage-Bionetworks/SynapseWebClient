package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Account;
import org.sagebionetworks.web.client.place.Settings;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.AccountView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class AccountPresenter extends AbstractActivity implements AccountView.Presenter, Presenter<Account> {
		
	private Account place;
	private AccountView view;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalAppState;
	private AuthenticationController authController;
	
	@Inject
	public AccountPresenter(AccountView view, 
			SynapseClientAsync synapseClient, 
			GlobalApplicationState globalAppState,
			AuthenticationController authController){
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalAppState = globalAppState;
		this.authController = authController;
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(Account place) {
		this.place = place;
		this.view.setPresenter(this);
		String emailValidationToken = place.toToken();
		synapseClient.addEmail(emailValidationToken, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				//success, send to Settings to view account state
				view.showInfo(DisplayConstants.EMAIL_SUCCESS, "");
				globalAppState.getPlaceChanger().goTo(new Settings(ClientProperties.DEFAULT_PLACE_TOKEN));
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorInPage(DisplayConstants.EMAIL_FAILURE, caught.getMessage());
			}
		});
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
}
