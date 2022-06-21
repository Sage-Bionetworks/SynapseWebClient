package org.sagebionetworks.web.client.widget.sharing;

import com.google.gwt.user.client.ui.IsWidget;

public interface OpenDataView extends IsWidget {
	void reset();
	void setIsPublicAndOpenAndAdmin();
	void setIsPublicAndAdmin();
	void setIsPrivateAndOpenAndAdmin();
}
