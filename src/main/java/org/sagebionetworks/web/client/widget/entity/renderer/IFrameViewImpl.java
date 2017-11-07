package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.web.client.widget.entity.PreviewWidgetViewImpl._autoAdjustFrameHeight;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class IFrameViewImpl extends FlowPanel implements IFrameView {

	@Inject
	public IFrameViewImpl() {
	}
	
	@Override
	public void configure(String siteUrl, int height) {
		this.clear();
		add(getFrame(siteUrl, height));
	}	
	
	private Frame getFrame(String siteUrl, int height) {
		final Frame frame = new Frame("about:blank");
		frame.getElement().setAttribute("frameborder", "0");
		frame.setWidth("100%");
		frame.setHeight(height + "px");
		frame.setUrl(siteUrl);
		return frame;
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override
	public void showInvalidSiteUrl(String siteUrl) {
		this.clear();
		add(new HTMLPanel(DisplayUtils.getMarkdownWidgetWarningHtml(siteUrl + DisplayConstants.INVALID_SHINY_SITE)));
	}		
	
	/*
	 * Private Methods
	 */

}
