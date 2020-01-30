package org.sagebionetworks.web.client;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This view is shown while the main block of code for Synapse is downloading and starting.
 * 
 * @author John
 *
 */
public class AppLoadingView extends PopupPanel {
	private final FlowPanel container = new FlowPanel();

	public AppLoadingView() {
		/**
		 * DO NOT ADD THIS TO THE MAIN IMAGE BUNGLE! This is shown while the image bundle is being download
		 * to the browser so putting it in the bundle defeats the whole purpose of this image.
		 */
		final Image ajaxImage = new Image("images/main-page-load.gif");
		final Grid grid = new Grid(1, 2);
		grid.setWidget(0, 0, ajaxImage);
		grid.setHTML(0, 1, "&nbsp;Loading...");
		this.container.add(grid);
		this.setStyleName("mainPageLoading");
		add(this.container);
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	public void stopProcessing() {
		hide();
	}

	public void startProcessing() {
		center();
		show();
	}

	public void showWidget() {
		startProcessing();
	}
}
