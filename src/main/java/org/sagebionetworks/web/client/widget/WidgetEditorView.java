package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.web.client.SynapseView;

public interface WidgetEditorView extends SynapseView {
	public void initView();

	@Deprecated
	public void checkParams() throws IllegalArgumentException;
}
