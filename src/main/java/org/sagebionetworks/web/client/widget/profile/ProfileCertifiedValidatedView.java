package org.sagebionetworks.web.client.widget.profile;

import org.sagebionetworks.web.client.widget.lazyload.SupportsLazyLoadInterface;

import com.google.gwt.user.client.ui.IsWidget;

public interface ProfileCertifiedValidatedView extends IsWidget, SupportsLazyLoadInterface {
	void setCertifiedVisible(boolean visible);
	void setVerifiedVisible(boolean visible);
	void setError(String error);
}
