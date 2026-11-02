package org.sagebionetworks.web.client.context;

import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.KeyFactory;
import org.sagebionetworks.web.client.jsinterop.KeyFactorySession;
import org.sagebionetworks.web.client.security.AuthenticationController;

public class KeyFactoryProviderImpl implements KeyFactoryProvider {

  private final AuthenticationController authController;

  @Inject
  public KeyFactoryProviderImpl(AuthenticationController authController) {
    this.authController = authController;
  }

  @Override
  public KeyFactory getKeyFactory(String currentUserAccessToken) {
    // Must describe the session the same way as the SynapseContext from SynapseReactClientFullContextPropsProvider,
    // so these keys match the ones SRC components use.
    return new KeyFactory(
      currentUserAccessToken,
      KeyFactorySession.create(
        authController.isLoggedIn(),
        authController.getCurrentUserRealmId()
      )
    );
  }
}
