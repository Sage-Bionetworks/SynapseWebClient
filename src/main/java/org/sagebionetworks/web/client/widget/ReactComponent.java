package org.sagebionetworks.web.client.widget;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.List;
import jsinterop.base.JsPropertyMap;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactComponentProps;
import org.sagebionetworks.web.client.jsinterop.ReactComponentType;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.ReactDOMRoot;
import org.sagebionetworks.web.client.jsinterop.ReactElement;

/**
 * Widget that manages the lifecycle of a React component. To use this widget, create a {@link ReactElement} using the
 * {@link React} API and call {@link #render(ReactElement)} to render the React component.
 * <p>
 * This widget also manages appending child elements if the associated React component can contain children. If all
 * child widgets use this class, then the child {@link ReactElement}s will be cloned and passed as children to the React
 * component. If any child of this component is a non-ReactComponent widget, then the child widgets will be injected
 * into the node found using the component's `ref`.
 * <p>
 * The root element defaults to a `div`, but can be changed (e.g. to a `span`) using the {@link #ReactComponent(String)} constructor.
 * <p>
 * This widget automatically unmounts the ReactComponent (if any) when this container is detached/unloaded.
 */
public class ReactComponent<T extends ReactComponentProps>
  extends ComplexPanel
  implements HasClickHandlers {

  private ReactDOMRoot root;
  private ReactComponentType<T> reactComponentType;
  public T props;

  private ReactElement<?> reactElement;

  public ReactComponent() {
    this(DivElement.TAG);
  }

  public ReactComponent(String tag) {
    setElement(Document.get().createElement(tag));
  }

  private boolean allChildrenAreReactComponents() {
    boolean allChildrenAreReactComponents = getChildren().size() > 0;
    for (Widget w : getChildren()) {
      if (!(w instanceof ReactComponent)) {
        allChildrenAreReactComponents = false;
        break;
      }
    }
    return allChildrenAreReactComponents;
  }

  private boolean isRenderedAsReactComponentChild() {
    return (
      getParent() instanceof ReactComponent &&
      ((ReactComponent<?>) getParent()).allChildrenAreReactComponents()
    );
  }

  /**
   * This method returns the root ReactComponent widget, which is the only place where this React tree is attached to the DOM.
   */
  private ReactComponent<?> getRootReactComponentWidget() {
    if (isRenderedAsReactComponentChild()) {
      return ((ReactComponent<?>) getParent()).getRootReactComponentWidget();
    } else {
      return this;
    }
  }

  @Override
  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return addDomHandler(handler, ClickEvent.getType());
  }

  private void maybeCreateRoot() {
    if (root == null && !isRenderedAsReactComponentChild()) {
      root = ReactDOM.createRoot(this.getElement());
    }
  }

  /**
   * Override the current props of the React component.
   * Because re-rendering the component will use `React.cloneElement`, old props must be explicitly set to `undefined`
   * to remove them.
   */
  public void overrideProps(T props) {
    this.props = props;
    this.rerender();
  }

  /**
   * Injects the GWT children into the React component. If all children are ReactComponents,
   * they will be cloned and added as React children.
   */
  private void injectChildWidgetsIntoComponent() {
    if (this.allChildrenAreReactComponents()) {
      // If all widget children are ReactElements, clone the React component and add them as children
      List<ReactComponent<?>> childWidgets = new ArrayList<>();
      getChildren().forEach(w -> childWidgets.add(((ReactComponent<?>) w)));

      ReactElement<?>[] childReactElements = childWidgets
        .stream()
        .map(ReactComponent::getReactElement)
        .toArray(ReactElement<?>[]::new);

      this.reactElement =
        React.cloneElement(reactElement, this.props, childReactElements);
    } else if (getChildren().size() > 0) {
      // Create a callback ref that will allow us to inject the GWT children into the DOM
      ReactComponentProps.CallbackRef refCallback = (Element node) -> {
        if (node != null) {
          // Once the DOM node is defined, inject each child
          getChildren().forEach(w -> node.appendChild(w.getElement()));
        }
      };

      if (this.props == null) {
        this.props = (T) JsPropertyMap.of();
      }
      this.props.ref = refCallback;

      this.reactElement =
        React.cloneElement(
          reactElement,
          // Override the ref
          this.props
        );
    }
  }

  public void render(ReactElement<?> reactElement) {
    this.reactElement = reactElement;
    maybeCreateRoot();
    injectChildWidgetsIntoComponent();

    // This component may be a React child of another component. If so, we must rerender the ancestor component(s) so
    // that they use the new ReactElement created in this render step.
    ReactComponent<?> componentToRender = getRootReactComponentWidget();
    if (componentToRender == this) {
      // Resynchronize with the DOM
      root.render(this.reactElement);
    } else {
      // Walk up the tree to the parent and repeat rerendering
      componentToRender.rerender();
    }
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    // Re-render the element
    this.rerender();
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    maybeCreateRoot();
    this.rerender();
  }

  @Override
  protected void onUnload() {
    super.onUnload();
    ReactDOMRoot rootToUnmount = this.root;
    // Immediately unbind this.root so synchronous attempts to re-render succeed
    this.root = null;

    // Asynchronously schedule unmounting the old root to allow React to finish a render cycle that might be in progress.
    // https://github.com/facebook/react/issues/25675
    if (rootToUnmount != null) {
      Timer t = new Timer() {
        @Override
        public void run() {
          rootToUnmount.unmount();
        }
      };
      t.schedule(0);
    }
  }

  /**
   * Adds a child widget.
   *
   * @param child the widget to be added
   * @throws UnsupportedOperationException if this method is not supported (most
   *           often this means that a specific overload must be called)
   */
  @Override
  public void add(Widget child) {
    // See implementation in com.google.gwt.user.client.ui.ComplexPanel

    // Detach new child
    child.removeFromParent();

    // Logical attach
    getChildren().add(child);

    // Physical attach (via React API!)
    if (reactElement != null) {
      // Rerender if possible
      this.render(reactElement);
    }

    // Adopt.
    adopt(child);
  }

  @Override
  public boolean remove(Widget w) {
    // See implementation in ComplexPanel

    // Validate.
    if (w.getParent() != this) {
      return false;
    }
    // Orphan.
    try {
      orphan(w);
    } finally {
      // Note - compared to ComplexPanel, we flipped logical and physical detach
      // This is because our render implementation depends on logical attachment

      // Logical detach.
      getChildren().remove(w);

      // Physical detach (via React API!)
      this.rerender();
    }
    return true;
  }

  public ReactElement<?> getReactElement() {
    return reactElement;
  }

  public void rerender() {
    if (reactElement != null) {
      this.render(reactElement);
    }
  }
}
