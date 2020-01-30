package org.sagebionetworks.web.client.widget;

import com.google.gwt.user.client.ui.IsWidget;

public interface CopyTextModal extends IsWidget {

	void setTitle(String title);

	void setText(String text);

	void show();
}
