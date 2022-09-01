package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;

import com.google.gwt.user.client.ui.IsWidget;

public interface OAuthClientEditorView extends IsWidget {
    void createReactComponentWidget(SynapseContextPropsProvider propsProvider);
}
