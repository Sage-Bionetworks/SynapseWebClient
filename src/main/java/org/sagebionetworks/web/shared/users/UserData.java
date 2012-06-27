package org.sagebionetworks.web.shared.users;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.web.client.cookie.CookieUtils;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This class should be removed and replaced with UserProfile. the isSSO flag and token should go into the global application state.
 * @author dburdick
 *
 */
@Deprecated
public class UserData implements IsSerializable {
	
	// The internal user id.
	private String email;
	// The display name for this user.
	private String userName;
	// The user's token
	private String token;
	private boolean isSSO = false;
	private String principalId;

	private static final int PRINCIPAL_COOKIE_IDX = 0;
	private static final int EMAIL_COOKIE_IDX = 1;
	private static final int USERNAME_COOKIE_IDX = 2;
	private static final int TOKEN_COOKIE_IDX = 3;
	private static final int ISSSO_COOKIE_IDX = 4;
	
	public static UserData getInstanceFromCookie(String cookieString) {
		List<String> cookieList = CookieUtils.createListFromString(cookieString);
		if (cookieList.size() == 5
				&& validateFields(cookieList.get(PRINCIPAL_COOKIE_IDX),
						cookieList.get(EMAIL_COOKIE_IDX),
						cookieList.get(USERNAME_COOKIE_IDX),
						cookieList.get(TOKEN_COOKIE_IDX))) {			
			
			return new UserData(cookieList.get(PRINCIPAL_COOKIE_IDX),
					cookieList.get(EMAIL_COOKIE_IDX),
					cookieList.get(USERNAME_COOKIE_IDX),
					cookieList.get(TOKEN_COOKIE_IDX), Boolean.parseBoolean(cookieList
							.get(ISSSO_COOKIE_IDX)));

		} 
		return null;
	}
	
	/*
	 * Default Constructor is required
	 */
	public UserData() {		
	}
	
	public UserData(String principalId, String email, String userName, String token, boolean isSSO) {
		this.principalId = principalId;
		this.email = email;
		this.userName = userName;
		this.token = token;
		this.isSSO = isSSO;
	}
		
	public String getCookieString() {
		// Add the fileds to a list
		List<String> fieldList = new ArrayList<String>();
		fieldList.add(principalId);
		fieldList.add(email);
		fieldList.add(userName);
		fieldList.add(token);
		fieldList.add(Boolean.toString(isSSO));
		return CookieUtils.createStringFromList(fieldList);
	}

	public String getPrincipalId() {
		return principalId;
	}

	public void setPrincipalId(String principalId) {
		this.principalId = principalId;
	}

	public String getEmail() {
		return email;
	}
	
	public String getUserName() {
		return userName;
	}

	public String getToken() {
		return token;
	}

	public boolean isSSO() {
		return isSSO;
	}

	public void setSSO(boolean isSSO) {
		this.isSSO = isSSO;
	}


	/*
	 * Private Methods
	 */
	private static boolean validateFields(String principalId, String email, String userName, String token) {
		if(principalId == null) return false;
		if(email == null) return false;
		if(userName == null) return false;
		if(token == null) return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + (isSSO ? 1231 : 1237);
		result = prime * result
				+ ((principalId == null) ? 0 : principalId.hashCode());
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		result = prime * result
				+ ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserData other = (UserData) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (isSSO != other.isSSO)
			return false;
		if (principalId == null) {
			if (other.principalId != null)
				return false;
		} else if (!principalId.equals(other.principalId))
			return false;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "UserData [email=" + email + ", userName=" + userName
				+ ", token=" + token + ", isSSO=" + isSSO + ", principalId="
				+ principalId + "]";
	}

	
}
