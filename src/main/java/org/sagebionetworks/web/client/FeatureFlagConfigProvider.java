package org.sagebionetworks.web.client;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.shared.WebConstants;

public class FeatureFlagConfigProvider implements Provider<FeatureFlagConfig> {

  CookieProvider cookieProvider;

  @Inject
  public FeatureFlagConfigProvider(CookieProvider cookieProvider) {
    this.cookieProvider = cookieProvider;
  }

  @Override
  public FeatureFlagConfig get() {
    return new FeatureFlagConfig(
      cookieProvider.getCookie(WebConstants.PORTAL_FEATURE_FLAG),
      cookieProvider
    );
  }
}
