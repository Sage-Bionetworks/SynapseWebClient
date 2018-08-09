package org.sagebionetworks.web.client.widget.clienthelp;

import com.google.gwt.user.client.ui.IsWidget;

public interface FileClientsHelpView extends IsWidget {
	void configure(String entityId, Long version);
	void setVersionVisible(boolean visible);
}
