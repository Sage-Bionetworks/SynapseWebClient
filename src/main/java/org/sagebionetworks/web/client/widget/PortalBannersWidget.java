package org.sagebionetworks.web.client.widget;

import com.google.gwt.dom.client.DivElement;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SynapsePortalBannersProps;

public class PortalBannersWidget extends ReactComponent {

  private String entityId;

  public PortalBannersWidget() {
    super(DivElement.TAG);
    addStyleName("portal-banners-widget");
  }

  public void configure(String entityId) {
    this.entityId = entityId;
    renderComponent();
  }

  private void renderComponent() {
    SynapsePortalBannersProps props = SynapsePortalBannersProps.create(
      entityId
    );
    ReactElement component = React.createElementWithThemeContext(
      SRC.SynapseComponents.SynapsePortalBanners,
      props
    );

    this.render(component);
  }
}
