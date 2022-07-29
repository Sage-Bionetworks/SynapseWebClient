package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;

import com.google.gwt.user.client.ui.IsWidget;

public interface TrashView extends IsWidget {
	void createReactComponentWidget(SynapseContextPropsProvider propsProvider);
}
