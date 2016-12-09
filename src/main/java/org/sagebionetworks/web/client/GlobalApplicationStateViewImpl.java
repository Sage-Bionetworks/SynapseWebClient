package org.sagebionetworks.web.client;

import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.options.DialogOptions;

public class GlobalApplicationStateViewImpl implements
		GlobalApplicationStateView {
	private static final int MONTH = 1000*60*60*24*7*4; //about a month
	
	@Override
	public void showVersionOutOfDateGlobalMessage() {
		DisplayUtils.showError(DisplayConstants.NEW_VERSION_AVAILABLE, DisplayConstants.NEW_VERSION_INSTRUCTIONS, MONTH);
	}
	
	@Override
	public void initGlobalViewProperties() {
		DialogOptions options = DialogOptions.newOptions("");
		options.setAnimate(false);
		Bootbox.setDefaults(options);
	}
}
