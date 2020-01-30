package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class IFrameViewImpl extends FlowPanel implements IFrameView {

	@Inject
	public IFrameViewImpl() {}

	@Override
	public int getParentOffsetHeight() {
		if (getParent() != null) {
			return getParent().getOffsetHeight();
		}
		return 0;
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
	public void showError(String error) {
		this.clear();
		add(new HTMLPanel(DisplayUtils.getMarkdownWidgetWarningHtml(error)));
	}

	/*
	 * Private Methods
	 */

}
