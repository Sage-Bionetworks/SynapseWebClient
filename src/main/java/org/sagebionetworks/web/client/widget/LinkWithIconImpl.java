package org.sagebionetworks.web.client.widget;

import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.LinkWithIconProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;

public class LinkWithIconImpl extends ReactComponent {

  private final SynapseReactClientFullContextPropsProvider contextPropsProvider;

  @Inject
  public LinkWithIconImpl(
    SynapseReactClientFullContextPropsProvider contextPropsProvider
  ) {
    this.contextPropsProvider = contextPropsProvider;
  }

  public void configure(String text, String icon, String href) {
    LinkWithIconProps props = LinkWithIconProps.create(text, icon, href);
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.LinkWithIcon,
      props,
      contextPropsProvider.getJsInteropContextProps()
    );
    this.render(component);
  }
}
