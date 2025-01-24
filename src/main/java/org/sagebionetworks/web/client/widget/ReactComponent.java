package org.sagebionetworks.web.client.widget;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.ReactDOMRoot;
import org.sagebionetworks.web.client.jsinterop.ReactElement;

/**
 * Automatically unmounts the ReactComponent (if any) inside this div when this container is detached/unloaded.
 */
public class ReactComponent extends FlowPanel implements HasClickHandlers {

  private ReactDOMRoot root;
  private ReactElement<?, ?> reactElement;

  public ReactComponent() {
    super(DivElement.TAG);
  }

  public ReactComponent(String tag) {
    super(tag);
  }

  private void createRoot() {
    if (root == null) {
      root = ReactDOM.createRoot(this.getElement());
    }
  }

  /**
   * Asynchronously (in the task queue, via setTimeout) unmounts the root and sets it to null.
   */
  private void destroyRoot() {
    // React itself may have fired this method in its render cycle. If that's the case, we cannot unmount synchronously.
    // We can asynchronously schedule unmounting the root to allow React to finish the current render cycle.
    // https://github.com/facebook/react/issues/25675
    Timer t = new Timer() {
      @Override
      public void run() {
        if (root != null) {
          root.unmount();
          root = null;
        }
      }
    };
    t.schedule(0);
  }

  /**
   * Asynchronously (in the task queue, via setTimeout) creates a root (if necessary) and renders the current reactElement.
   */
  private void createRootAndRender() {
    // Asynchronously schedule createRoot and render to ensure any prequeued `destroyRoot` task completes first
    Timer t = new Timer() {
      @Override
      public void run() {
        createRoot();
        // Resynchronize with the DOM
        root.render(reactElement);
      }
    };
    t.schedule(0);
  }

  public void render(ReactElement<?, ?> reactElement) {
    this.reactElement = reactElement;
    createRootAndRender();
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    createRoot();

    if (reactElement != null) {
      this.render(reactElement);
    }
  }

  @Override
  protected void onUnload() {
    destroyRoot();
    super.onUnload();
  }

  @Override
  public void clear() {
    if (root != null) {
      root.render(React.createElement(React.Fragment));
    }
  }

  @Override
  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return addDomHandler(handler, ClickEvent.getType());
  }
}
