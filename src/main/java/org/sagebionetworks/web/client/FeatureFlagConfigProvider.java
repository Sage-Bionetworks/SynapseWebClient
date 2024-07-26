package org.sagebionetworks.web.client;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.shared.WebConstants;

public class FeatureFlagConfigProvider implements Provider<FeatureFlagConfig> {

  private final CookieProvider cookieProvider;
  private final SessionStorage sessionStorage;

  @Inject
  public FeatureFlagConfigProvider(
    CookieProvider cookieProvider,
    SessionStorage sessionStorage
  ) {
    this.cookieProvider = cookieProvider;
    this.sessionStorage = sessionStorage;
  }

  @Override
  public FeatureFlagConfig get() {
    return new FeatureFlagConfig(
      sessionStorage.getItem(
        WebConstants.PORTAL_FEATURE_FLAG_SESSION_STORAGE_KEY
      ),
      cookieProvider
    );
  }
}
