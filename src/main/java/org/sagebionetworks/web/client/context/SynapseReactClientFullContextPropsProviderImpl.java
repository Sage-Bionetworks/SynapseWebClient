package org.sagebionetworks.web.client.context;

import javax.inject.Inject;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.OneSageUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.jsinterop.ApplicationSessionContextJsObject;
import org.sagebionetworks.web.client.jsinterop.IsEditingStore;
import org.sagebionetworks.web.client.jsinterop.SynapseContextJsObject;
import org.sagebionetworks.web.client.jsinterop.SynapseReactClientFullContextProviderProps;
import org.sagebionetworks.web.client.jsinterop.SynapseSessionManagerJs;
import org.sagebionetworks.web.client.security.AuthenticationController;

public class SynapseReactClientFullContextPropsProviderImpl
  implements SynapseReactClientFullContextPropsProvider {

  private final AuthenticationController authController;
  private final GlobalApplicationState globalApplicationState;
  private final CookieProvider cookies;
  private final QueryClientProvider queryClientProvider;
  private final OneSageUtils oneSageUtils;

  @Inject
  SynapseReactClientFullContextPropsProviderImpl(
    final AuthenticationController authController,
    final GlobalApplicationState globalApplicationState,
    final CookieProvider cookies,
    final QueryClientProvider queryClientProvider,
    final OneSageUtils oneSageUtils
  ) {
    this.authController = authController;
    this.globalApplicationState = globalApplicationState;
    this.cookies = cookies;
    this.queryClientProvider = queryClientProvider;
    this.oneSageUtils = oneSageUtils;
  }

  @Override
  public SynapseReactClientFullContextProviderProps getJsInteropContextProps() {
    return SynapseReactClientFullContextProviderProps.create(
      SynapseContextJsObject.create(
        authController.getCurrentUserAccessToken(),
        authController.isLoggedIn(),
        DisplayUtils.isInTestWebsite(cookies),
        globalApplicationState.isShowingUTCTime(),
        oneSageUtils.getAppIdForOneSage()
      ),
      queryClientProvider.getQueryClient(),
      IsEditingStore.create(
        callback -> {
          Runnable unsubscribe =
            globalApplicationState.subscribeToIsEditingChange(callback::run);
          return unsubscribe::run;
        },
        globalApplicationState::isEditing,
        globalApplicationState::setIsEditing
      ),
      ApplicationSessionContextJsObject.create(
        authController.getCurrentUserAccessToken(),
        authController.getCurrentUserRealmId(),
        authController.getCurrentUserPrincipalId(),
        authController.isLoggedIn(),
        sessionManagerHasInitialized(),
        () -> authController.getSessionManager().refreshSession(),
        authController::logoutUser,
        false
      ) // isLoadingSSO
    );
  }

  private boolean sessionManagerHasInitialized() {
    SynapseSessionManagerJs mgr = authController.getSessionManager();
    if (mgr == null) {
      return false;
    }
    return mgr.getSnapshot().hasInitializedSession;
  }
}
