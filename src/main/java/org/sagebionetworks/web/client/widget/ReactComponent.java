package org.sagebionetworks.web.client.widget;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
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

  public void render(ReactElement<?, ?> reactElement) {
    this.reactElement = reactElement;

    // This component may be a React child of another component. If so, we must rerender the ancestor component(s) so
    // that they use the new ReactElement created in this render step.
    // Asynchronously schedule creating a root in case React is still rendering and may unmount the current root
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
    // An unmounted root cannot be re-used, so first clear out this.root. If this widget is re-loaded, a new root will be created.
    // Save a reference to the old root and schedule unmount asynchronously.
    ReactDOMRoot oldRoot = this.root;
    this.root = null;

    if (oldRoot != null) {
      // Asynchronously schedule unmounting the old root to allow React to finish the current render cycle.
      // https://github.com/facebook/react/issues/25675
      Timer t = new Timer() {
        @Override
        public void run() {
          oldRoot.unmount();
        }
      };
      t.schedule(0);
    }
    super.onUnload();
  }

  @Override
  public void clear() {
    // clear doesn't typically call onUnload, but we want to for this element.
    this.onUnload();
    super.clear();
  }

  @Override
  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return addDomHandler(handler, ClickEvent.getType());
  }
}
