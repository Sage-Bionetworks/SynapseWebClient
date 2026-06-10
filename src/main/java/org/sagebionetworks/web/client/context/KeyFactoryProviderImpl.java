package org.sagebionetworks.web.client.context;

import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.KeyFactory;

public class KeyFactoryProviderImpl implements KeyFactoryProvider {

  @Inject
  public KeyFactoryProviderImpl() {}

  @Override
  public KeyFactory getKeyFactory(String currentUserAccessToken) {
    return new KeyFactory(currentUserAccessToken);
  }
}
