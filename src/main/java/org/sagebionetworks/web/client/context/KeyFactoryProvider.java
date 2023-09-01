package org.sagebionetworks.web.client.context;

import org.sagebionetworks.web.client.jsinterop.KeyFactory;

public interface KeyFactoryProvider {
  public KeyFactory getKeyFactory(String currentUserAccessToken);
}
