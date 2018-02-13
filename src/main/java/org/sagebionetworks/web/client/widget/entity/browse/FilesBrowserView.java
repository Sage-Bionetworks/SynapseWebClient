package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface FilesBrowserView extends IsWidget, SynapseView {

	/**
	 * Configure the view with the parent id
	 * @param entityId
	 */
	void configure(String entityId);
	void refreshTreeView(String entityId);
}
