package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import org.sagebionetworks.web.client.SynapseView;

public interface EntityPageTopView extends IsWidget, SynapseView {
  void setProjectMetadata(Widget w);

  void setTabs(Widget w);
  void setProjectTitleBar(IsWidget w);
  void setProjectActionMenu(Widget w);

  void setProjectLoadingVisible(boolean visible);
  void setProjectUIVisible(boolean visible);

  void scrollToTop();

  EventBinder<EntityPageTop> getEventBinder();
}
