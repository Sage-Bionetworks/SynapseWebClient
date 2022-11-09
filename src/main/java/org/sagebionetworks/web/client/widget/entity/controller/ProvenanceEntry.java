package org.sagebionetworks.web.client.widget.entity.controller;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.utils.Callback;

public interface ProvenanceEntry extends IsWidget {
  public void setRemoveCallback(Callback removalCallback);

  public void setAnchorTarget(String targetURL);
}
