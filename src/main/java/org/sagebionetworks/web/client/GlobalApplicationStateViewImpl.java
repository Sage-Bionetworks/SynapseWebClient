package org.sagebionetworks.web.client;

import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.options.DialogOptions;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;

public class GlobalApplicationStateViewImpl implements
		GlobalApplicationStateView {
	private static final int UNLIMITED_TIME = 0;
	Frame iframe;
	@Override
	public void showVersionOutOfDateGlobalMessage() {
		DisplayUtils.showError(DisplayConstants.NEW_VERSION_AVAILABLE + DisplayConstants.NEW_VERSION_INSTRUCTIONS, UNLIMITED_TIME);
		preloadNewVersion();
	}
	@Override
	public void showGetVersionError(String error) {
		DisplayUtils.showError("Unable to determine the Synapse version. Please refresh the page to get the latest version. " + error, 5000);
		preloadNewVersion();
	}
	@Override
	public void initGlobalViewProperties() {
		DialogOptions options = DialogOptions.newOptions("");
		options.setAnimate(false);
		Bootbox.setDefaults(options);
	}
	
	public void preloadNewVersion() {
		//preload, after a (10 minute) delay
		Timer timer = new Timer() { 
		    public void run() {
		    	if (iframe != null) {
					RootPanel.getBodyElement().removeChild(iframe.getElement());
				}
				String currentURL = Window.Location.getHref();
				iframe = new Frame(currentURL);
				iframe.setWidth("1px");
				iframe.setHeight("1px");
				RootPanel.getBodyElement().appendChild(iframe.getElement());
				iframe.setVisible(false);    	
		    } 
		};
		timer.schedule(1000*60*10);
	}
}
