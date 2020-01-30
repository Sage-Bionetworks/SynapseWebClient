package org.sagebionetworks.web.client.widget.clienthelp;

import com.google.gwt.user.client.ui.IsWidget;

public interface FileViewClientsHelp extends IsWidget {
	void setQuery(String sql);

	void show();
}
