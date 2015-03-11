package org.sagebionetworks.web.client.widget.provenance;

import org.gwtbootstrap3.client.ui.Popover;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Trigger;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class ProvNodeContainer extends FlowPanel {	
	
	HTML messageContent;
	Popover messagePopup;
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
		tip = new Tooltip(content);
		tip.setTrigger(Trigger.MANUAL);
		tip.setIsHtml(true);
		tip.setPlacement(Placement.BOTTOM);
		this.add(tip);
		setupMessage();
	}
	
	public FlowPanel getContent() {
		return content;
	}
	
	public void showMessage(String display, String detailedMessage) {
		SafeHtmlBuilder html = new SafeHtmlBuilder();
		if(display != null) html.appendHtmlConstant(display);
		messageContent.setHTML(html.toSafeHtml());
		messagePopup.setWidget(new HTML(detailedMessage));
		messageContainer.setVisible(true);
	}

	public Tooltip getTip() {
		return tip;
	}
	
	private void setupMessage() {
		messageContainer = new FlowPanel();				
		messageContent = new HTML();
		messageContainer.add(messageContent);
		messagePopup = DisplayUtils.addPopover(messageContainer, "");
		this.add(messageContainer);
		messageContainer.setVisible(false);
	}
	
}
