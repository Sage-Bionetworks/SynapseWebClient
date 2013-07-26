package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;

public class BookmarkWidgetViewImpl extends LayoutContainer implements BookmarkWidgetView {
	private Presenter presenter;
	private String bookmarkID;
	private String bookmarkLinkText;
	private boolean hasLoaded;
	
	@Inject
	public BookmarkWidgetViewImpl() {
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;		
	}

	@Override
	public void configure(String bookmarkID, String bookmarkLinkText) {
		this.removeAll();
		this.bookmarkID = bookmarkID;
		this.bookmarkLinkText = bookmarkLinkText;
		hasLoaded = false;
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		if(!hasLoaded) {
			hasLoaded = true;
			HTMLPanel parentPanel = (HTMLPanel)this.getParent();
			Element heading = parentPanel.getElementById(bookmarkID);
			final Element scrollToElement = heading;
			Anchor a = new Anchor();
			a.setHTML(bookmarkLinkText);
			a.addStyleName("link");
			a.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					Window.scrollTo(0, scrollToElement.getOffsetTop());
				}
			});
			add(a);
			layout(true);		
		}
	}

}
