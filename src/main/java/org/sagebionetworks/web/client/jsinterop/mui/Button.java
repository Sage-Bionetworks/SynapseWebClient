package org.sagebionetworks.web.client.jsinterop.mui;

import org.sagebionetworks.web.client.jsinterop.IconSvgProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactComponentType;
import org.sagebionetworks.web.client.jsinterop.SRC;

public class Button
  extends HasSx<ReactComponentType<ButtonProps>, ButtonProps> {

  public Button() {
    super(MaterialUI.Button, new ButtonProps());
  }

  public void setId(String id) {
    props.id = id;
    this.render();
  }

  public void setVariant(String variant) {
    props.variant = variant;
    this.render();
  }

  public void setColor(String color) {
    props.color = color;
    this.render();
  }

  public void setSize(String size) {
    props.size = size;
    this.render();
  }

  public void setStartIcon(String iconName) {
    IconSvgProps iconProps = IconSvgProps.create(iconName, null);
    props.startIcon =
      React.createElement(SRC.SynapseComponents.IconSvg, iconProps);
    this.render();
  }

  public void setHref(String href) {
    props.href = href;
    this.render();
  }

  public void setFullWidth(boolean fullWidth) {
    props.fullWidth = fullWidth;
    this.render();
  }

  public void setDisabled(boolean disabled) {
    props.disabled = disabled;
    this.render();
  }

  public void setTarget(String target) {
    props.target = target;
    this.render();
  }

  public void setChildren(String children) {
    props.children = children;
    this.render();
  }
}
