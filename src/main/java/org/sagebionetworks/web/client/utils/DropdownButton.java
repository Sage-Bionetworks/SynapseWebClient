package org.sagebionetworks.web.client.utils;

import org.sagebionetworks.web.client.DisplayUtils.ButtonType;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;

public class DropdownButton extends FlowPanel {

	private Anchor btn;
	private UnorderedListPanel ul;
	int count = 0;
	ClickHandler toggleClickHandler;
	public DropdownButton(String title, ButtonType type) {
		this(title, type, null);
	}
	
	public DropdownButton(String title, ButtonType type, String iconClass) {
		addStyleName("dropdown");
		btn = new Anchor();
		btn.removeStyleName("gwt-Button");
		btn.addStyleName("btn btn-" + type.toString().toLowerCase() + " dropdown-toggle");
		String style = iconClass == null ? "" : " class=\"glyphicon " + iconClass+ "\"" ;
		btn.setHTML(SafeHtmlUtils.fromSafeConstant(
				"<span" + style+ "></span> " + 
				title +
				" <span class=\"caret\"></span> "));
//		btn.getElement().setAttribute("data-toggle", "dropdown");
		
		add(btn);
		
		ul = new UnorderedListPanel();
		ul.addStyleName("displayInline dropdown-menu");
		
		add(ul);
		ul.setVisible(false);
		toggleClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ul.setVisible(!ul.isVisible());
			}
		};
		btn.addClickHandler(toggleClickHandler);
	}

	public void addMenuItem(Anchor a) {
		ul.add(a);
		a.addClickHandler(toggleClickHandler);
		count++;
	}
	
	public void addMenuItem(Hyperlink a) {
		ul.add(a);
		a.addClickHandler(toggleClickHandler);
		count++;
	}
	
	public void addDivider() {
		ul.add("divider");
		count++;
	}
	
	public int getCount() {
		return count;
	}
}
