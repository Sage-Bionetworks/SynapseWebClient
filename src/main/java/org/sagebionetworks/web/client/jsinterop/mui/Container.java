package org.sagebionetworks.web.client.jsinterop.mui;

import org.sagebionetworks.web.client.jsinterop.ReactComponentType;

public class Container
  extends HasSx<ReactComponentType<ContainerProps>, ContainerProps> {

  public Container() {
    super(MaterialUI.Container, new ContainerProps());
  }

  public void setMaxWidth(String maxWidth) {
    props.maxWidth = maxWidth;
    this.render();
  }

  public void setDisableGutters(boolean disableGutters) {
    props.disableGutters = disableGutters;
    this.render();
  }

  public void setFixed(boolean fixed) {
    props.fixed = fixed;
    this.render();
  }

  public void setChildren(String children) {
    props.children = children;
    this.render();
  }
}
