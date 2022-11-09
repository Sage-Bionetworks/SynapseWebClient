package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.SynapseView;

public interface SharingAndDataUseConditionWidgetView
  extends IsWidget, SynapseView {
  void configure(EntityBundle bundle);
}
