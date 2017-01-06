package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.UIObject;

public class ElementWrapper {
	Element element;
	
	public ElementWrapper(Element element) {
		this.element = element;
	}
	
	public Element getElement() {
		return element;
	}
	
	public String getAttribute(String attr) {
		return element.getAttribute(attr);
	}
	
	public void setAttribute(String attr, String value) {
		element.setAttribute(attr, value);
	}
	
	public void removeAllChildren() {
		element.removeAllChildren();
	}
	
	public void setVisible(boolean visible) {
		UIObject.setVisible(element, visible);
	}
	
}
