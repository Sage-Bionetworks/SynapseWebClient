package org.sagebionetworks.web.client.widget;

public interface WidgetDescriptorView extends SynapseWidgetView {
	public void initView();
	public int getDisplayHeight();
	public void checkParams() throws IllegalArgumentException;
}
