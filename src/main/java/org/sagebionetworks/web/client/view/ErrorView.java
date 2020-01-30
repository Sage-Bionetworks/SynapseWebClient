package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface ErrorView extends IsWidget {
	void setErrorMessage(String errorMessage);

	void refreshHeader();
}
