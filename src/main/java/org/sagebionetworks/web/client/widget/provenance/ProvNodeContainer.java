package org.sagebionetworks.web.client.widget.provenance;

import org.sagebionetworks.web.client.DisplayUtils;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class ProvNodeContainer extends LayoutContainer {	
	
	HTML messageContent;
	PopupPanel messagePopup;
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
		messagePopup = DisplayUtils.addToolTip(messageContainer, "");
		this.add(messageContainer);
		this.layout(true);
		messageContainer.hide();
	}
	
}
