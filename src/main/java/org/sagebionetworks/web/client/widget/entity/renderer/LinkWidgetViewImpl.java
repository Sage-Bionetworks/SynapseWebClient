package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.inject.Inject;

public class LinkWidgetViewImpl extends LayoutContainer implements LinkWidgetView {
	private Presenter presenter;
	
	@Inject
	public LinkWidgetViewImpl() {	
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void configure(String linkText, String linkUrl) {
		removeAll();
		Anchor a = new Anchor();
		final String decodedUrl = WidgetEncodingUtil.decodeValue(linkUrl);
		linkText = WidgetEncodingUtil.decodeValue(linkText);
		a.setHTML(linkText);
		a.addStyleName("link");
		//if this is a custom Synapse link, set the href to open sub-url
		if(decodedUrl.contains("Synapse:")) {
			a.setHref(decodedUrl);
			a.setTarget("_blank");
		} else {
			//go to the full url
			a.addClickHandler(new ClickHandler() {			
				@Override
				public void onClick(ClickEvent event) {
					DisplayUtils.newWindow(decodedUrl, "", "");
				}
			});
		}
		add(a);
		layout(true);
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}
}
