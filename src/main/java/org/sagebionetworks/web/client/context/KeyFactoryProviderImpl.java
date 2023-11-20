package org.sagebionetworks.web.client.context;

import org.sagebionetworks.web.client.jsinterop.KeyFactory;

public class KeyFactoryProviderImpl implements KeyFactoryProvider {

  @Override
  public KeyFactory getKeyFactory(String currentUserAccessToken) {
    return new KeyFactory(currentUserAccessToken);
  }
}
