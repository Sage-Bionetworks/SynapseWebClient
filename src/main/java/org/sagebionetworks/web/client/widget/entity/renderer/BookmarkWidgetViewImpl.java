package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.SynapseJSNIUtilsImpl;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;

public class BookmarkWidgetViewImpl extends FlowPanel implements BookmarkWidgetView {
	private Presenter presenter;
	private String bookmarkID;
	private String bookmarkLinkText;
	
	@Inject
	public BookmarkWidgetViewImpl() {
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;		
	}

	@Override
	public void configure(final String bookmarkID, String bookmarkLinkText) {
		this.clear();
		this.bookmarkID = bookmarkID;
		this.bookmarkLinkText = bookmarkLinkText;
		
		Anchor a = new Anchor();
		a.setHTML(SimpleHtmlSanitizer.sanitizeHtml(bookmarkLinkText));
		a.addStyleName("link");
		a.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				HTMLPanel parentPanel = (HTMLPanel)getParent();
				Element heading = parentPanel.getElementById(bookmarkID);
				final Element scrollToElement = heading;
				SynapseJSNIUtilsImpl._scrollIntoView(scrollToElement);
			}
		});
		add(a);
	}
}
