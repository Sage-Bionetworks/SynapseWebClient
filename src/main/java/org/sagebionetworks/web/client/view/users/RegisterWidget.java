package org.sagebionetworks.web.client.view.users;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.MembershipInvtnSignedToken;
import org.sagebionetworks.repo.model.auth.NewUser;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.exceptions.ConflictException;

public class RegisterWidget implements RegisterWidgetView.Presenter, SynapseWidgetPresenter {
	
	private RegisterWidgetView view;
	private UserAccountServiceAsync userService;
	private GWTWrapper gwt;
	private SynapseAlert synAlert;
	private MembershipInvtnSignedToken membershipInvtnSignedToken;

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

	/**
	 * Create the new user account
	 * @param newUser
	 */
	@Override
	public void registerUser(NewUser newUser) {
		synAlert.clear();
		view.enableRegisterButton(false);
		String callbackUrl = gwt.getHostPageBaseURL() + "#!NewAccount:";
		userService.createUserStep1(newUser, callbackUrl, new AsyncCallback<Void>() {
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

	@Override
	public MembershipInvtnSignedToken getMembershipInvtnSignedToken() {
		return membershipInvtnSignedToken;
	}

	public void setMembershipInvtnSignedToken(MembershipInvtnSignedToken membershipInvtnSignedToken) {
		this.membershipInvtnSignedToken = membershipInvtnSignedToken;
	}

	public void setVisible(boolean isVisible) {
		view.setVisible(isVisible);
	}
	
	public void setEmail(String email) {
		view.setEmail(email);
	}
}
