package org.sagebionetworks.web.client.widget.oauthclient;

import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class OAuthClientEditor extends ReactComponent {

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
