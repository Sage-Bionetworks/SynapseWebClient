package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.repo.model.SignedTokenInterface;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Account;
import org.sagebionetworks.web.client.place.Profile;
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

	@Inject
	public AccountPresenter(AccountView view, SynapseClientAsync synapseClient, GlobalApplicationState globalAppState) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.globalAppState = globalAppState;
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
		synapseClient.hexDecodeAndDeserialize(place.toToken(), new AsyncCallback<SignedTokenInterface>() {
			@Override
			public void onSuccess(SignedTokenInterface result) {
				if (result instanceof EmailValidationSignedToken) {
					addEmail((EmailValidationSignedToken) result);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ACCOUNT_CREATION_FAILURE + caught.getMessage());
			}
		});
	}

	public void addEmail(EmailValidationSignedToken emailValidationSignedToken) {
		synapseClient.addEmail(emailValidationSignedToken, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				// success, send to Settings to view account state
				view.showInfo(DisplayConstants.EMAIL_SUCCESS);
				globalAppState.getPlaceChanger().goTo(new Profile(Profile.EDIT_PROFILE_TOKEN));
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
