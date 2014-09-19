package org.sagebionetworks.web.client.presenter.users;

import org.sagebionetworks.repo.model.principal.AliasType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.Presenter;
import org.sagebionetworks.web.client.view.users.RegisterAccountView;
import org.sagebionetworks.web.shared.exceptions.ConflictException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class RegisterAccountPresenter extends AbstractActivity implements RegisterAccountView.Presenter, Presenter<RegisterAccount> {
	public static final String KEY_DATASETS_SELECTED_COLUMNS_COOKIE = "org.sagebionetworks.selected.dataset.columns";
	
	private RegisterAccount place;
	private RegisterAccountView view;
	private UserAccountServiceAsync userService;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private GWTWrapper gwt;
	
	@Inject
	public RegisterAccountPresenter(RegisterAccountView view,
			UserAccountServiceAsync userService,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			GWTWrapper gwt) {
		this.view = view;
		// Set the presenter on the view
		this.userService = userService;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.gwt = gwt;
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}

	@Override
	public void setPlace(RegisterAccount place) {
		this.place = place;
		view.setPresenter(this);
		view.showDefault();
	}

	/**
	 * check that the email is available
	 * @param username
	 * @param email
	 * @param firstName
	 * @param lastName
	 */
	public void checkEmailAvailable(String email) {
		synapseClient.isAliasAvailable(email, AliasType.USER_EMAIL.toString(), new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean isAvailable) {
				if (!isAvailable)
					view.markEmailUnavailable();
			}
			
			@Override
			public void onFailure(Throwable e) {
				//do nothing.  validation has failed, but updating the email will fail if it's already taken.
				e.printStackTrace();
			}
		});
	}

	/**
	 * Create the new user account
	 * @param username
	 * @param email
	 * @param firstName
	 * @param lastName
	 */
	@Override
	public void registerUser(String email) {
		String callbackUrl = gwt.getHostPageBaseURL() + "#!NewAccount:";
		userService.createUserStep1(email, callbackUrl, new AsyncCallback<Void>() {			
			@Override
			public void onSuccess(Void result) {
				view.showAccountCreated();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(caught instanceof ConflictException) {
					view.markEmailUnavailable();
				} else {
					if (!DisplayUtils.handleServiceException(caught, globalApplicationState, false, view))
						view.showErrorMessage(DisplayConstants.ERROR_GENERIC_NOTIFY);
				}
			}
		});
	}
	
}
