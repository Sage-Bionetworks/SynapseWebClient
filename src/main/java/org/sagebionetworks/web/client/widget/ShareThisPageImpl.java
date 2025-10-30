package org.sagebionetworks.web.client.widget;

import com.google.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.ShareThisPageProps;
import org.sagebionetworks.web.client.jsinterop.ShareThisPageProps.Callback;

public class ShareThisPageImpl extends ReactComponent implements ShareThisPage {

  @Inject
  public ShareThisPageImpl() {}

  public void configure(
    String shortIoPublicApiKey,
    String domain,
    boolean open,
    Callback onClose,
    String renderAs
  ) {
    ShareThisPageProps props = ShareThisPageProps.create();
    props.setOpen(open);
    props.setOnClose(onClose);
    props.setShortIoPublicApiKey(shortIoPublicApiKey);
    props.setDomain(domain);
    props.setRenderAs(renderAs);
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.ShareThisPage,
      props
    );
    this.render(component);
  }
}
