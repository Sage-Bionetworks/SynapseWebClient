package org.sagebionetworks.web.client.widget.provenance;

import org.gwtbootstrap3.client.ui.Popover;
import org.sagebionetworks.web.client.DisplayUtils;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class ProvNodeContainer extends LayoutContainer {	
	
	HTML messageContent;
	Popover messagePopup;
	LayoutContainer messageContainer;
	
	public ProvNodeContainer() {
		super();
	}
	
	private LayoutContainer content;
	
	public void addContent(Widget widget) {
		if(widget instanceof LayoutContainer) {
			this.content = (LayoutContainer) widget;
			this.add(widget);
		} else {
			this.content = new LayoutContainer();
			content.add(widget);
			this.add(content);
		}
		setupMessage();
	}
	
	public Component getContent() {
		return content;
	}
	
	public void showMessage(String display, String detailedMessage) {
		SafeHtmlBuilder html = new SafeHtmlBuilder();
		if(display != null) html.appendHtmlConstant(display);
		messageContent.setHTML(html.toSafeHtml());
		messagePopup.setWidget(new HTML(detailedMessage));
		messageContainer.show();
	}

	private void setupMessage() {
		messageContainer = new LayoutContainer();				
		messageContent = new HTML();
		messageContainer.add(messageContent);
		messagePopup = DisplayUtils.addPopover(messageContainer, "");
		this.add(messageContainer);
		this.layout(true);
		messageContainer.hide();
	}
	
}
