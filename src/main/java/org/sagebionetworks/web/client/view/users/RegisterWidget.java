package org.sagebionetworks.web.client.view.users;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.exceptions.ConflictException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RegisterWidget implements RegisterWidgetView.Presenter, SynapseWidgetPresenter {
	
	private RegisterWidgetView view;
	private UserAccountServiceAsync userService;
	private GWTWrapper gwt;
	private SynapseAlert synAlert;

	@Inject
	public RegisterWidget(RegisterWidgetView view, 
			UserAccountServiceAsync userService,
			GWTWrapper gwt, 
			SynapseAlert synAlert) {
		this.view = view;
		this.userService = userService;
		this.gwt = gwt;
		this.synAlert = synAlert;
		view.setPresenter(this);
		view.setSynAlert(synAlert.asWidget());
	}	
	
	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();		
	}

	public void configure(boolean isInline) {
		view.setInlineUI(isInline);
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
		synAlert.clear();
		view.enableRegisterButton(false);
		String callbackUrl = gwt.getHostPageBaseURL() + "#!NewAccount:";
		userService.createUserStep1(email, callbackUrl, new AsyncCallback<Void>() {			
			@Override
			public void onSuccess(Void result) {
				view.enableRegisterButton(true);
				view.showInfo(DisplayConstants.ACCOUNT_EMAIL_SENT);
				view.clear();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.enableRegisterButton(true);
				if(caught instanceof ConflictException) {
					synAlert.showError(DisplayConstants.ERROR_EMAIL_ALREADY_EXISTS);
				} else {
					synAlert.handleException(caught);
				}
			}
		});
	}

	public void setVisible(boolean isVisible) {
		view.setVisible(isVisible);
	}
	
	public void setEmail(String email) {
		view.setEmail(email);
	}
}
