package org.sagebionetworks.web.client.context;

import javax.inject.Inject;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.jsinterop.SynapseContextJsObject;
import org.sagebionetworks.web.client.jsinterop.SynapseContextProviderProps;
import org.sagebionetworks.web.client.jsni.QueryClientJSNIObject;
import org.sagebionetworks.web.client.jsni.SynapseContextJSNIObject;
import org.sagebionetworks.web.client.jsni.SynapseContextProviderPropsJSNIObject;
import org.sagebionetworks.web.client.security.AuthenticationController;

public class SynapseContextPropsProviderImpl implements SynapseContextPropsProvider {
    private AuthenticationController authController;
    private GlobalApplicationState globalApplicationState;
    private CookieProvider cookies;
	private QueryClientProvider queryClientProvider;


    @Inject
    SynapseContextPropsProviderImpl(final AuthenticationController authController, final GlobalApplicationState globalApplicationState, final CookieProvider cookies, final QueryClientProvider queryClientProvider) {
        this.authController = authController;
        this.globalApplicationState = globalApplicationState;
        this.cookies = cookies;
		this.queryClientProvider = queryClientProvider;
    }

    @Override
    public SynapseContextProviderProps getJsInteropContextProps() {
        return SynapseContextProviderProps.create(
                SynapseContextJsObject.create(
                    authController.getCurrentUserAccessToken(),
                    DisplayUtils.isInTestWebsite(cookies),
                    globalApplicationState.isShowingUTCTime()
                ),
                queryClientProvider.getQueryClient()
        );
    }

    @Override
    public SynapseContextProviderPropsJSNIObject getJsniContextProps() {
        SynapseContextJSNIObject synapseContext = SynapseContextJSNIObject.create();
        synapseContext.setAccessToken(authController.getCurrentUserAccessToken());
        synapseContext.setIsInExperimentalMode(DisplayUtils.isInTestWebsite(cookies));
        synapseContext.setUtcTime(globalApplicationState.isShowingUTCTime());

        SynapseContextProviderPropsJSNIObject props = SynapseContextProviderPropsJSNIObject.create();
        props.setSynapseContext(synapseContext);
        props.setQueryClient(QueryClientJSNIObject.getQueryClientSingleton());
        return props;
    }
}
