package org.sagebionetworks.web.client.context;

import javax.inject.Inject;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.jsinterop.SynapseContextJsObject;
import org.sagebionetworks.web.client.jsinterop.SynapseContextProviderProps;
import org.sagebionetworks.web.client.jsinterop.reactquery.QueryClient;
import org.sagebionetworks.web.client.jsinterop.reactquery.QueryClientOptions;
import org.sagebionetworks.web.client.jsni.QueryClientJSNIObject;
import org.sagebionetworks.web.client.jsni.SynapseContextJSNIObject;
import org.sagebionetworks.web.client.jsni.SynapseContextProviderPropsJSNIObject;
import org.sagebionetworks.web.client.security.AuthenticationController;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;

public class SynapseContextPropsProviderImpl implements SynapseContextPropsProvider {
    private AuthenticationController authController;
    private GlobalApplicationState globalApplicationState;
    private CookieProvider cookies;

    private static QueryClient queryClientSingleton = new QueryClient(QueryClientOptions.create());

    // We save the queryClient in a global variable so we can access it from JSNI
    @JsProperty(namespace = JsPackage.GLOBAL, name="SynapseQueryClient")
    static native void setQueryClient(QueryClient queryClient);

    @Inject
    SynapseContextPropsProviderImpl(final AuthenticationController authController, final GlobalApplicationState globalApplicationState, final CookieProvider cookies) {
        this.authController = authController;
        this.globalApplicationState = globalApplicationState;
        this.cookies = cookies;
        setQueryClient(queryClientSingleton);
    }

    @Override
    public SynapseContextProviderProps getJsInteropContextProps() {
        return SynapseContextProviderProps.create(
                SynapseContextJsObject.create(
                    authController.getCurrentUserAccessToken(),
                    DisplayUtils.isInTestWebsite(cookies),
                    globalApplicationState.isShowingUTCTime()
                ),
                queryClientSingleton
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
