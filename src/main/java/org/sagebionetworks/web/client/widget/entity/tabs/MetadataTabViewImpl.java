package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.MetadataTasksPage;

public class MetadataTabViewImpl implements MetadataTabView {

  private final SimplePanel container;
  private MetadataTasksPage reactComponent;
  // Incremented on every call to configure() and used as the React `key` prop, forcing the component to remount
  // (rather than reconcile) each time the tab is shown. This resets any internal state, notably the internal
  // router's current route, so returning to the tab always starts from its base route.
  private int keyCounter = 0;

  @Inject
  MetadataTabViewImpl() {
    container = new SimplePanel();
    container.addStyleName("margin-top-15 entity-page-side-margins");
  }

  @Override
  public void configure(String projectId) {
    keyCounter++;
    String key = String.valueOf(keyCounter);
    if (reactComponent == null) {
      reactComponent = new MetadataTasksPage(projectId);
      container.setWidget(reactComponent.asWidget());
    }
    reactComponent.setProjectIdAndKey(projectId, key);
    reactComponent.render();
  }

  @Override
  public Widget asWidget() {
    return container.asWidget();
  }
}
