package org.sagebionetworks.web.client.widget.provenance;

import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Trigger;

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
	Tooltip tip;
	
	public ProvNodeContainer() {
		super();
	}
	
	private FlowPanel content;
	
	public void addContent(Widget widget) {
		if(widget instanceof FlowPanel) {
			this.content = (FlowPanel) widget;
		} else {
			this.content = new FlowPanel();
			content.add(widget);
		}
		
		setupTooltip("");
		setupMessage();
	}
	
	public void setupTooltip(String title) {
		if (tip != null) {
			this.remove(tip);
		}
		tip = new Tooltip(content);
		tip.setIsAnimated(false);
		tip.setIsHtml(true);
		tip.setTitle(title);
		tip.setPlacement(Placement.RIGHT);
		tip.setTrigger(Trigger.HOVER);
		tip.addTooltipInnerClassName("width-200");
		
		this.add(tip);
	}

	public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
		return addDomHandler(handler, MouseOverEvent.getType());
	}
	public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
		return addDomHandler(handler, MouseOutEvent.getType());
	}
	
	public void showTooltip() {
		if (tip != null) {
			tip.show();	
		}
	}
	
	public void showMessage(String display) {
		SafeHtmlBuilder html = new SafeHtmlBuilder();
		if(display != null) html.appendHtmlConstant(display);
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
