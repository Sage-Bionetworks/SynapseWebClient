package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.SynapseJSNIUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;

public class BookmarkWidgetViewImpl extends FlowPanel implements BookmarkWidgetView {
	SynapseJSNIUtils jsniUtils;

	@Inject
	public BookmarkWidgetViewImpl(SynapseJSNIUtils jsniUtils) {
		this.jsniUtils = jsniUtils;
	}

	@Override
	public void configure(final String bookmarkID, String bookmarkLinkText) {
		this.clear();

		Anchor a = new Anchor();
		a.setHTML(SimpleHtmlSanitizer.sanitizeHtml(bookmarkLinkText));
		a.addStyleName("link");
		a.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				HTMLPanel parentPanel = (HTMLPanel) getParent();
				Element heading = parentPanel.getElementById(bookmarkID);
				final Element scrollToElement = heading;
				jsniUtils.scrollIntoView(scrollToElement);
			}
		});
		add(a);
	}
}
