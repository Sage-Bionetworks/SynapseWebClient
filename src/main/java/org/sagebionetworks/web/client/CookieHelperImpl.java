package org.sagebionetworks.web.client;

import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;

import com.google.inject.Inject;

/**
 * A super lightweight class for dealing with cookies
 * 
 * **** DO NOT ADD NODE MODEL CREATOR OR ANY REPO MODEL OBJECTS TO THIS CLASS ****
 * (for code splitting purposes)
 * 
 * @author dburdick
 *
 */
public class CookieHelperImpl implements CookieHelper {

	private static final String SESSION_TOKEN = "sessionToken";
	private CookieProvider cookies;
	private AdapterFactory adapterFactory;
	
	@Inject
	public CookieHelperImpl(CookieProvider cookies, AdapterFactory adapterFactory){
		this.cookies = cookies;
		this.adapterFactory = adapterFactory;
	}

	@Override
	public boolean isLoggedIn() {
		String loginCookieString = cookies.getCookie(CookieKeys.USER_LOGIN_DATA);
		if(loginCookieString != null) {
			// remo.model.UserSessionData object, without using it directly
			try {
				JSONObjectAdapter userSessionDataObject = adapterFactory.createNew(loginCookieString);
				if(userSessionDataObject.has(SESSION_TOKEN)) {
					String token = userSessionDataObject.get(SESSION_TOKEN).toString();
					if(token != null && !token.isEmpty())
						return true;
				}
			} catch (JSONObjectAdapterException e) {
			}			
		} 
		return false;
	}

}
