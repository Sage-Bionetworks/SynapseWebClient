package org.sagebionetworks.web.client.widget.entity.file;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.jsinterop.EntityPageTitleBarProps;

public interface BasicTitleBarView extends IsWidget, SynapseView {
  void setProps(EntityPageTitleBarProps props);
}
