package org.sagebionetworks.web.client.context;

import javax.inject.Inject;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.jsinterop.SynapseContextJsObject;
import org.sagebionetworks.web.client.jsinterop.SynapseReactClientFullContextProviderProps;
import org.sagebionetworks.web.client.jsni.FullContextProviderPropsJSNIObject;
import org.sagebionetworks.web.client.jsni.QueryClientJSNIObject;
import org.sagebionetworks.web.client.jsni.SynapseReactClientFullContextJSNIObject;
import org.sagebionetworks.web.client.security.AuthenticationController;

public class SynapseReactClientFullContextPropsProviderImpl
  implements SynapseReactClientFullContextPropsProvider {

  private AuthenticationController authController;
  private GlobalApplicationState globalApplicationState;
  private CookieProvider cookies;
  private QueryClientProvider queryClientProvider;

  @Inject
  SynapseReactClientFullContextPropsProviderImpl(
    final AuthenticationController authController,
    final GlobalApplicationState globalApplicationState,
    final CookieProvider cookies,
    final QueryClientProvider queryClientProvider
  ) {
    this.authController = authController;
    this.globalApplicationState = globalApplicationState;
    this.cookies = cookies;
    this.queryClientProvider = queryClientProvider;
  }

  @Override
  public SynapseReactClientFullContextProviderProps getJsInteropContextProps() {
    return SynapseReactClientFullContextProviderProps.create(
      SynapseContextJsObject.create(
        authController.getCurrentUserAccessToken(),
        DisplayUtils.isInTestWebsite(cookies),
        globalApplicationState.isShowingUTCTime()
      ),
      queryClientProvider.getQueryClient()
    );
  }

  @Override
  public FullContextProviderPropsJSNIObject getJsniContextProps() {
    SynapseReactClientFullContextJSNIObject synapseContext = SynapseReactClientFullContextJSNIObject.create();
    synapseContext.setAccessToken(authController.getCurrentUserAccessToken());
    synapseContext.setIsInExperimentalMode(
      DisplayUtils.isInTestWebsite(cookies)
    );
    synapseContext.setUtcTime(globalApplicationState.isShowingUTCTime());
    synapseContext.setDownloadCartPageUrl("/#!DownloadCart:0");

    FullContextProviderPropsJSNIObject props = FullContextProviderPropsJSNIObject.create();
    props.setSynapseContext(synapseContext);
    props.setQueryClient(QueryClientJSNIObject.getQueryClientSingleton());
    return props;
  }
}
