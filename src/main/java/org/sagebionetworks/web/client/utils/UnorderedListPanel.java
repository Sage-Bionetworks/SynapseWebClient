package org.sagebionetworks.web.client.utils;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

public class UnorderedListPanel extends ComplexPanel {
	final UListElement ul = Document.get().createULElement();	

	public UnorderedListPanel() {
		setElement(ul);
	}

	public void add(Widget w) {
		addLi(w);
	}

	public void add(Widget w, String styleName) {
		LIElement li = addLi(w);
		li.setClassName(styleName);
	}
	
	private LIElement addLi(Widget w) {
		LIElement li = Document.get().createLIElement();		
		ul.appendChild(li);
		add(w, (Element) li.cast());	
		return li;
	}

	public void insert(Widget w, int beforeIndex) {
		checkIndexBoundsForInsertion(beforeIndex);

		LIElement li = Document.get().createLIElement();
		ul.insertBefore(li, ul.getChild(beforeIndex));
		insert(w, (Element) li.cast(), beforeIndex, false);
	}

	public boolean remove(Widget w) {
		Element li = DOM.getParent(w.getElement());
		boolean removed = super.remove(w);
		if (removed) {
			ul.removeChild(li);
		}
		return removed;
	}
}
