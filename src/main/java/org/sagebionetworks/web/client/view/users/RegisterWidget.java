package org.sagebionetworks.web.client.view.users;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.client.ValidationUtils.isValidEmail;
import org.sagebionetworks.repo.model.auth.NewUser;
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
	private String encodedMembershipInvtnSignedToken;

	@Inject
	public RegisterWidget(RegisterWidgetView view, UserAccountServiceAsync userService, GWTWrapper gwt, SynapseAlert synAlert) {
		this.view = view;
		this.userService = userService;
		fixServiceEntryPoint(userService);
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
	 * 
	 * @param email
	 */
	@Override
	public void registerUser(String email) {
		NewUser newUser = new NewUser();
		if (!isValidEmail(email)) {
			synAlert.showError(DisplayConstants.INVALID_EMAIL);
			return;
		}
		newUser.setEmail(email);
		newUser.setEncodedMembershipInvtnSignedToken(encodedMembershipInvtnSignedToken);
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
				if (caught instanceof ConflictException) {
					synAlert.showError(DisplayConstants.ERROR_EMAIL_ALREADY_EXISTS);
				} else {
					synAlert.handleException(caught);
				}
			}
		});
	}

	@Override
	public String getEncodedMembershipInvtnSignedToken() {
		return encodedMembershipInvtnSignedToken;
	}

	public void setEncodedMembershipInvtnSignedToken(String encodedMembershipInvtnSignedToken) {
		this.encodedMembershipInvtnSignedToken = encodedMembershipInvtnSignedToken;
	}

	public void setVisible(boolean isVisible) {
		view.setVisible(isVisible);
	}

	public void setEmail(String email) {
		view.setEmail(email);
	}

	public void enableEmailAddressField(boolean enabled) {
		view.enableEmailAddressField(enabled);
	}
}
