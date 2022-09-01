package org.sagebionetworks.web.client.widget.oauthclient;

import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class OAuthClientEditor extends ReactComponentDiv {
    public OAuthClientEditor(SynapseContextPropsProvider contextPropsProvider){
        ReactDOM.render(
                React.createElementWithSynapseContext(
                        SRC.SynapseComponents.OAuthManagement,
                        null,
                        contextPropsProvider.getJsInteropContextProps()
                ),
                getElement()
        );
    }
}