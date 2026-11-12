package org.sagebionetworks.web.client.widget.search;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SearchConfigBindingModalProps;
import org.sagebionetworks.web.client.widget.ReactComponent;

/**
 * Modal for viewing and changing the SearchConfiguration bound to an entity.
 */
public class SearchConfigBindingModalWidget implements IsWidget {

  private final ReactComponent reactComponent;
  private String entityId;
  private SearchConfigBindingModalProps.Callback onClose;

  @Inject
  public SearchConfigBindingModalWidget() {
    reactComponent = new ReactComponent();
  }

  public void configure(
    String entityId,
    SearchConfigBindingModalProps.Callback onClose
  ) {
    reactComponent.clear();
    this.entityId = entityId;
    this.onClose = onClose;
    renderComponent(false);
  }

  public void setOpen(boolean open) {
    renderComponent(open);
  }

  private void renderComponent(boolean open) {
    ReactElement reactElement = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SearchConfigBindingModal,
      SearchConfigBindingModalProps.create(open, entityId, onClose)
    );
    reactComponent.render(reactElement);
  }

  @Override
  public Widget asWidget() {
    return reactComponent.asWidget();
  }
}
