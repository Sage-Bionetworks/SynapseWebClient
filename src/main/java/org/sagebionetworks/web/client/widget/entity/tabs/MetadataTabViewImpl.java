package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.MetadataTasksPage;

public class MetadataTabViewImpl implements MetadataTabView {

  private final SimplePanel container;
  private MetadataTasksPage reactComponent;

  @Inject
  MetadataTabViewImpl() {
    container = new SimplePanel();
    container.addStyleName("margin-top-15 entity-page-side-margins");
  }

  @Override
  public void configure(String projectId) {
    if (reactComponent == null) {
      reactComponent = new MetadataTasksPage(projectId);
      container.setWidget(reactComponent.asWidget());
    } else {
      reactComponent.setProjectId(projectId);
    }
    reactComponent.render();
  }

  @Override
  public Widget asWidget() {
    return container.asWidget();
  }
}
