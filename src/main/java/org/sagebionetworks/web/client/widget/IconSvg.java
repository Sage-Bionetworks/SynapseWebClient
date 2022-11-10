package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.web.client.jsinterop.IconSvgProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;

public class IconSvg extends ReactComponentSpan {

  private String icon;

  private String label;

  public IconSvg() {}

  public void configure(String icon, String label) {
    this.icon = icon;
    this.label = label;
    renderComponent();
  }

  private void renderComponent() {
    IconSvgProps props = IconSvgProps.create(icon, label);
    ReactNode component = React.createElementWithThemeContext(
      SRC.SynapseComponents.IconSvg,
      props
    );
    this.render(component);
  }

  public void setIcon(String icon) {
    this.icon = icon;
    renderComponent();
  }
}
