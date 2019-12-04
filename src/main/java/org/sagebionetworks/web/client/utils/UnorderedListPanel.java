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

	public void addStyleName(String style) {
		ul.setClassName(ul.getClassName() + " " + style);
	}

	public void setAttribute(String name, String value) {
		ul.setAttribute(name, value);
	}

	public void add(Widget w) {
		addLi(w);
	}

	public void add(String styleName) {
		add(null, styleName);
	}

	public void add(Widget w, String styleName) {
		LIElement li = addLi(w);
		li.setClassName(styleName);
	}

	public int indexOf(Widget w) {
		return this.getWidgetIndex(w);
	}

	private LIElement addLi(Widget w) {
		LIElement li = Document.get().createLIElement();
		ul.appendChild(li);
		if (w != null)
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

	/**
	 * Adds the the given style name to the list item that corresponds to the given widget.
	 * 
	 * @param w
	 * @param styleName
	 * @return If there is no list item that corresponds to the given widget or the list item already
	 *         had the given style name, returns false. Else, returns true.
	 */
	public static boolean addStyleNameToListItem(Widget w, String styleName) {
		Element li = DOM.getParent(w.getElement());
		if (li == null) {
			return false;
		}
		return li.addClassName(styleName);
	}

	/**
	 * Removes the the given style name to the list item that corresponds to the given widget.
	 * 
	 * @param w
	 * @param styleName
	 * @return If there is no list item that corresponds to the given widget or the list item already
	 *         had the given style name, returns false. Else, returns true.
	 */
	public static boolean removeStyleNameFromListItem(Widget w, String styleName) {
		Element li = DOM.getParent(w.getElement());
		if (li == null) {
			return false;
		}
		return li.removeClassName(styleName);
	}
}
