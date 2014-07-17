package org.sagebionetworks.web.client.view.bootstrap.table;

import org.gwtbootstrap3.client.ui.base.ComplexWidget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.HasText;

/**
 * Simple <th>
 * 
 * @author jmhill
 *
 */
public class TableHeader extends ComplexWidget implements HasText {

    public TableHeader() {
        setElement(Document.get().createTHElement());
    }

	@Override
	public String getText() {
		return getElement().getInnerText();
	}

	@Override
	public void setText(String text) {
		getElement().setInnerText(text);
	}
}