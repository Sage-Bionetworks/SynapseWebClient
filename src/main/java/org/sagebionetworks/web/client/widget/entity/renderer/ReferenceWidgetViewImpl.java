package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.shared.WidgetConstants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ReferenceWidgetViewImpl extends FlowPanel implements ReferenceWidgetView {
	
	private Presenter presenter;
	private String id;
	
	@Inject
	public ReferenceWidgetViewImpl() {
	}
	
	@Override
	public void configure(String footnoteId) {
		this.clear();
		id = footnoteId;
		
		
		HTMLPanel parentPanel = (HTMLPanel)this.getParent();
		Element heading = parentPanel.getElementById(WidgetConstants.FOOTNOTE_ID_WIDGET_PREFIX + id);
		final Element scrollToElement = heading;
		Anchor a = new Anchor();
		a.setHTML("[" + id + "]");
		a.addStyleName("link");
		a.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.scrollTo(0, scrollToElement.getOffsetTop());
			}
		});
		add(a);
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

}
