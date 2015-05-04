package org.sagebionetworks.web.client.view.users;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.exceptions.ConflictException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RegisterWidget implements RegisterWidgetView.Presenter, SynapseWidgetPresenter {
	
	private RegisterWidgetView view;
	private GlobalApplicationState globalApplicationState;
	private UserAccountServiceAsync userService;
	private GWTWrapper gwt;

	@Inject
	public RegisterWidget(RegisterWidgetView view, 
			UserAccountServiceAsync userService,
			GlobalApplicationState globalApplicationState,
			GWTWrapper gwt) {
		this.view = view;
		this.userService = userService;
		this.globalApplicationState = globalApplicationState;
		this.gwt = gwt;
		view.setPresenter(this);
	}	
	
	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();		
	}

	public void clearState() {
		view.clear();
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
		view.enableRegisterButton(false);
		String callbackUrl = gwt.getHostPageBaseURL() + "#!NewAccount:";
		userService.createUserStep1(email, callbackUrl, new AsyncCallback<Void>() {			
			@Override
			public void onSuccess(Void result) {
				view.enableRegisterButton(true);
				view.showInfo(DisplayConstants.ACCOUNT_EMAIL_SENT, "");
				clearState();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.enableRegisterButton(true);
				if(caught instanceof ConflictException) {
					view.showErrorMessage(DisplayConstants.ERROR_EMAIL_ALREADY_EXISTS);
				} else {
					if (!DisplayUtils.handleServiceException(caught, globalApplicationState, false, view))
						view.showErrorMessage(DisplayConstants.ERROR_GENERIC_NOTIFY);
				}
			}
		});
	}

	public void setVisible(boolean isVisible) {
		view.setVisible(isVisible);
	}

	
	/*
	 * Private Methods
	 */
}
