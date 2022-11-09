package org.sagebionetworks.web.client.widget.doi;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.SynapseView;

public interface DoiWidgetV2View extends IsWidget, SynapseView {
  void showDoi(String doiText);

  void hide();

  void setLabelVisible(boolean visible);
}
