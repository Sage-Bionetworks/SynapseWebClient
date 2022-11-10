package org.sagebionetworks.web.client.widget.sharing;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.SynapseView;

public interface PublicPrivateBadgeView extends IsWidget, SynapseView {
  void setIsPublic(boolean isPublic);
}
