package org.sagebionetworks.web.client.widget.entity.download;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * View class used to request credentials required to establish a connection to AWS (or a stack that
 * is AWS-like).
 * 
 * @author jayhodgson
 *
 */
public interface AwsLoginView extends IsWidget {
	void setEndpoint(String value);

	String getAccessKey();

	String getSecretKey();

	void clear();

	void setVisible(boolean visible);
}
