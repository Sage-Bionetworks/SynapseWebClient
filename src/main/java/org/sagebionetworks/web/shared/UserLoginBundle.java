package org.sagebionetworks.web.shared;

import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserSessionData;

import com.google.gwt.user.client.rpc.IsSerializable;

public class UserLoginBundle implements IsSerializable {

	private UserSessionData userSessionData;
	private UserBundle userBundle;
	
	public UserLoginBundle() {}
	
	public UserLoginBundle(UserSessionData userSessionData, UserBundle userBundle) {
		this.userSessionData = userSessionData;
		this.userBundle = userBundle;
	}
	
	public UserSessionData getUserSessionData() {
		return userSessionData;
	}
	
	public void setUserSessionData(UserSessionData userSessionData) {
		this.userSessionData = userSessionData;
	}
	
	public UserBundle getUserBundle() {
		return userBundle;
	}
	
	public void setUserBundle(UserBundle userBundle) {
		this.userBundle = userBundle;
	}
}
