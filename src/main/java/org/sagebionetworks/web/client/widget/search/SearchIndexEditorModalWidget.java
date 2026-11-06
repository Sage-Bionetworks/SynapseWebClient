package org.sagebionetworks.web.client.widget.search;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SearchIndexEditorModalProps;
import org.sagebionetworks.web.client.widget.ReactComponent;

/**
 * Modal for editing an existing SearchIndex's settings.
 */
public class SearchIndexEditorModalWidget implements IsWidget {

  private final ReactComponent reactComponent;
  private String entityId;
  private SearchIndexEditorModalProps.OnComplete onComplete;
  private SearchIndexEditorModalProps.Callback onCancel;

  @Inject
  public SearchIndexEditorModalWidget() {
    reactComponent = new ReactComponent();
  }

  public void configure(
    String entityId,
    SearchIndexEditorModalProps.OnComplete onComplete,
    SearchIndexEditorModalProps.Callback onCancel
  ) {
    reactComponent.clear();
    this.entityId = entityId;
    this.onComplete = onComplete;
    this.onCancel = onCancel;
    renderComponent(false);
  }

  public void setOpen(boolean open) {
    renderComponent(open);
  }

  private void renderComponent(boolean open) {
    ReactElement reactElement = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SearchIndexEditorModal,
      SearchIndexEditorModalProps.create(open, entityId, onComplete, onCancel)
    );
    reactComponent.render(reactElement);
  }

  @Override
  public Widget asWidget() {
    return reactComponent.asWidget();
  }
}
