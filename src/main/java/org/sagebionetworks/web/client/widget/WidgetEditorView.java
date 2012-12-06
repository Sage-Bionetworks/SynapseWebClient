package org.sagebionetworks.web.client.widget;

public interface WidgetEditorView extends SynapseWidgetView {
	public void initView();
	public int getDisplayHeight();
	public int getAdditionalWidth();
	public void checkParams() throws IllegalArgumentException;
}
