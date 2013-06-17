package org.sagebionetworks.web.client.factory;

import org.sagebionetworks.web.client.CookieHelper;
import org.sagebionetworks.web.client.cookie.CookieProvider;

public interface SystemFactory {

	public CookieProvider getCookieProvider();
	
	public CookieHelper getCookieHelper();
}
