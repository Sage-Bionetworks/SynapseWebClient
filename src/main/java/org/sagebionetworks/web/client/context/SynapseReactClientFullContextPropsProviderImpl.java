package org.sagebionetworks.web.client.context;

import javax.inject.Inject;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.OneSageUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.jsinterop.SynapseContextJsObject;
import org.sagebionetworks.web.client.jsinterop.SynapseReactClientFullContextProviderProps;
import org.sagebionetworks.web.client.jsni.FullContextProviderPropsJSNIObject;
import org.sagebionetworks.web.client.jsni.QueryClientJSNIObject;
import org.sagebionetworks.web.client.jsni.SynapseReactClientFullContextJSNIObject;
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
        DisplayUtils.isInTestWebsite(cookies),
        globalApplicationState.isShowingUTCTime(),
        oneSageUtils.getAppIdForOneSage()
      ),
      queryClientProvider.getQueryClient()
    );
  }

  @Override
  public FullContextProviderPropsJSNIObject getJsniContextProps() {
    SynapseReactClientFullContextJSNIObject synapseContext =
      SynapseReactClientFullContextJSNIObject.create();
    synapseContext.setAccessToken(authController.getCurrentUserAccessToken());
    synapseContext.setIsInExperimentalMode(
      DisplayUtils.isInTestWebsite(cookies)
    );
    synapseContext.setUtcTime(globalApplicationState.isShowingUTCTime());
    synapseContext.setDownloadCartPageUrl("/DownloadCart:0");

    FullContextProviderPropsJSNIObject props =
      FullContextProviderPropsJSNIObject.create();
    props.setSynapseContext(synapseContext);
    props.setQueryClient(QueryClientJSNIObject.getQueryClientSingleton());
    return props;
  }
}
