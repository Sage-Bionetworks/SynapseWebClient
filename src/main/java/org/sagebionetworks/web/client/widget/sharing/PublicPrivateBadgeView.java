package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;

public interface PublicPrivateBadgeView extends IsWidget, SynapseView {
	void setIsPublic(boolean isPublic);
}
