package org.sagebionetworks.web.client.widget.oauthclient;

import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class OAuthClientEditor extends ReactComponentDiv {

  public OAuthClientEditor(
    SynapseReactClientFullContextPropsProvider contextPropsProvider
  ) {
    this.render(
        React.createElementWithSynapseContext(
          SRC.SynapseComponents.OAuthManagement,
          null,
          contextPropsProvider.getJsInteropContextProps()
        )
      );
  }
}
