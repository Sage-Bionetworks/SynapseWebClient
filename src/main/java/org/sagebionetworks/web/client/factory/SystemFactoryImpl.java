package org.sagebionetworks.web.client.factory;

import org.sagebionetworks.web.client.CookieHelper;
import org.sagebionetworks.web.client.cookie.CookieProvider;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class SystemFactoryImpl implements SystemFactory {
	
	@Inject
	private Provider<CookieProvider> cookieProviderProvider;
	@Inject
	private Provider<CookieHelper> cookieHelperProvider;

	@Override
	public CookieProvider getCookieProvider() {
		return cookieProviderProvider.get();
	}

	@Override
	public CookieHelper getCookieHelper() {
		return cookieHelperProvider.get();
	}
}
