package org.sagebionetworks.web.client.widget;

import com.google.gwt.user.client.ui.InlineLabel;
import org.sagebionetworks.web.client.jsinterop.ReactDOMRoot;
import org.sagebionetworks.web.client.jsinterop.ReactNode;

/**
 * Automatically unmounts the ReactComponent (if any) inside this div when this container is detached/unloaded.
 */
public class ReactComponentSpan extends InlineLabel {

  private ReactDOMRoot root;
  private ReactNode component;

  public void render(ReactNode reactNode) {
    this.setComponent(reactNode);
    if (root == null) {
      root = ReactComponentLifecycleUtils.onLoad(this.getElement());
    }
    root.render(reactNode);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    if (root == null) {
      root = ReactComponentLifecycleUtils.onLoad(this.getElement());
    }
    if (component != null) {
      this.render(component);
    }
  }

  @Override
  protected void onUnload() {
    if (root != null) {
      ReactComponentLifecycleUtils.onUnload(root);
      root = null;
    }
    super.onUnload();
  }

  public void setComponent(ReactNode component) {
    this.component = component;
  }

  public ReactNode getComponent() {
    return component;
  }
}
