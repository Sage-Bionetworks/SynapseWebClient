package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.web.shared.WebConstants;

public class PortalPropertiesProviderImpl implements PortalPropertiesProvider {

  @Override
  public boolean getIsDevMode() {
    return "true".equals(
        PortalPropertiesHolder.getProperty(WebConstants.IS_DEV_MODE_KEY)
      );
  }
}
