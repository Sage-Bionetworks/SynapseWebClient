package org.sagebionetworks.web.client.widget.provenance;

import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class ProvNodeContainer extends FlowPanel implements HasMouseOverHandlers, HasMouseOutHandlers {

	HTML messageContent;
	FlowPanel messageContainer;

	public ProvNodeContainer() {
		super();
	}

	public void addContent(Widget widget) {
		this.add(widget);
		setupMessage();
	}

	public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
		return addDomHandler(handler, MouseOverEvent.getType());
	}

	public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
		return addDomHandler(handler, MouseOutEvent.getType());
	}

	public void showMessage(String display) {
		SafeHtmlBuilder html = new SafeHtmlBuilder();
		if (display != null)
			html.appendHtmlConstant(display);
		messageContent.setHTML(html.toSafeHtml());
		messageContainer.setVisible(true);
	}

	private void setupMessage() {
		messageContainer = new FlowPanel();
		messageContent = new HTML();
		messageContainer.add(messageContent);
		this.add(messageContainer);
		messageContainer.setVisible(false);
	}

}
