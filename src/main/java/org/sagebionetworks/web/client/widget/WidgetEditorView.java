package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.web.client.SynapseView;

public interface WidgetEditorView extends SynapseView {
	public void initView();
	public int getDisplayHeight();
	public int getAdditionalWidth();
	public void checkParams() throws IllegalArgumentException;
}
