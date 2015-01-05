package org.sagebionetworks.web.client.utils;

import org.sagebionetworks.web.client.DisplayUtils.ButtonType;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;

public class DropdownButton extends FlowPanel {

	private Button btn;
	private UnorderedListPanel ul;
	int count = 0;
	
	public DropdownButton(String title, ButtonType type) {
		this(title, type, null);
	}
	
	public DropdownButton(String title, ButtonType type, String iconClass) {
		btn = new Button();
		btn.removeStyleName("gwt-Button");
		btn.addStyleName("btn btn-" + type.toString().toLowerCase() + " dropdown-toggle");
		String style = iconClass == null ? "" : " class=\"glyphicon " + iconClass+ "\"" ;
		btn.setHTML(SafeHtmlUtils.fromSafeConstant(
				"<span" + style+ "></span> " + 
				title +
				" <span class=\"caret\"></span> "));
		btn.getElement().setAttribute("data-toggle", "dropdown");
		
		add(btn);
		
		ul = new UnorderedListPanel();
		ul.addStyleName("dropdown-menu");
		ul.setAttribute("role", "menu");
		add(ul);
	}

	public void addMenuItem(Anchor a) {
		ul.add(a);
		count++;
	}
	
	public void addMenuItem(Hyperlink a) {
		ul.add(a);
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
