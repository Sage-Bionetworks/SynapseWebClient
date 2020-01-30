package org.sagebionetworks.web.client.widget.profile;

import com.google.gwt.user.client.ui.IsWidget;

public interface ProfileCertifiedValidatedView extends IsWidget {
	void setCertifiedVisible(boolean visible);

	void setVerifiedVisible(boolean visible);

	void setError(String error);
}
