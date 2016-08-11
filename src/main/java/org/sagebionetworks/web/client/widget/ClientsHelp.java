package org.sagebionetworks.web.client.widget;

import com.google.gwt.user.client.ui.IsWidget;

public interface ClientsHelp extends IsWidget {
	void setVisible(boolean visible);
	void configure(String commandLine, String java, String python, String r);
}
