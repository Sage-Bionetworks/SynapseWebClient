package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.web.client.DisplayUtils;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;

public class IconMenu extends Composite {

	LayoutContainer container;

	public IconMenu() {
		container = new LayoutContainer();
		initWidget(container);
	}

	public void addIcon(ImageResource imageResource, String tooltip, ClickHandler handler) {
		Anchor createIconLink = DisplayUtils.createIconLink(AbstractImagePrototype.create(imageResource), handler);
		createIconLink.setTitle(tooltip);
		createIconLink.addStyleName("margin-right-5");
		container.add(createIconLink);
	}
}
