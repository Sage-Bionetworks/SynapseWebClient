package org.sagebionetworks.web.client;

import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.options.DialogOptions;

public class GlobalApplicationStateViewImpl implements
		GlobalApplicationStateView {
	private static final int UNLIMITED_TIME = 0;
	
	@Override
	public void showVersionOutOfDateGlobalMessage() {
		DisplayUtils.showError(DisplayConstants.NEW_VERSION_AVAILABLE, DisplayConstants.NEW_VERSION_INSTRUCTIONS, UNLIMITED_TIME);
	}
	
	@Override
	public void initGlobalViewProperties() {
		DialogOptions options = DialogOptions.newOptions("");
		options.setAnimate(false);
		Bootbox.setDefaults(options);
	}
}
