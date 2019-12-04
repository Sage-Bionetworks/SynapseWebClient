package org.sagebionetworks.web.client.widget.asynch;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.client.presenter.ProfilePresenter.IS_ACT_MEMBER;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.UserProfileClientAsync;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * Efficiently answer if this user is in the ACT. Note that if user manually changes the session
 * storage value, they will see ACT actions, but will be blocked by the services if they should
 * attempt to use the functionality.
 * 
 * @author Jay
 *
 */
public class IsACTMemberAsyncHandlerImpl implements IsACTMemberAsyncHandler {
	UserProfileClientAsync userProfileClient;
	SessionStorage sessionStorage;
	AuthenticationController authController;
	SynapseJSNIUtils jsniUtils;
	boolean visible = true;
	public static final String SESSION_KEY_PREFIX = "ACT_MEMBER_";

	@Inject
	public IsACTMemberAsyncHandlerImpl(UserProfileClientAsync userProfileClient, SessionStorage sessionStorage, AuthenticationController authController, SynapseJSNIUtils jsniUtils) {
		this.userProfileClient = userProfileClient;
		fixServiceEntryPoint(userProfileClient);
		this.sessionStorage = sessionStorage;
		this.authController = authController;
		this.jsniUtils = jsniUtils;
	}

	@Override
	public void isACTActionAvailable(final CallbackP<Boolean> callback) {
		isACTMember(new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean isACTMember) {
				callback.invoke(isACTMember && visible);
			}
		});
	}

	@Override
	public void isACTMember(final CallbackP<Boolean> callback) {
		if (!authController.isLoggedIn()) {
			callback.invoke(false);
			return;
		}

		String cachedValue = sessionStorage.getItem(SESSION_KEY_PREFIX + authController.getCurrentUserPrincipalId());
		if (cachedValue != null) {
			callback.invoke(Boolean.valueOf(cachedValue));
			return;
		}

		// do rpc
		userProfileClient.getMyOwnUserBundle(IS_ACT_MEMBER, new AsyncCallback<UserBundle>() {
			@Override
			public void onSuccess(UserBundle userBundle) {
				sessionStorage.setItem(SESSION_KEY_PREFIX + authController.getCurrentUserPrincipalId(), userBundle.getIsACTMember().toString());
				callback.invoke(userBundle.getIsACTMember());
			}

			@Override
			public void onFailure(Throwable caught) {
				// log the error, and tell client that user is not part of the ACT (give the developer something to
				// look at if something goes wrong for an ACT member)
				jsniUtils.consoleError(caught.getMessage());
				callback.invoke(false);
			}
		});
	}

	@Override
	public void setACTActionVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public boolean isACTActionVisible() {
		return visible;
	}
}
