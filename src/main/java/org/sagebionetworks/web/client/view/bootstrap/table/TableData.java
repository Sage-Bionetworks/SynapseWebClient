package org.sagebionetworks.web.client.view.bootstrap.table;

import org.gwtbootstrap3.client.ui.base.ComplexWidget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.HasText;

/**
 * Simple <td>
 * @author jmhill
 *
 */
public class TableData extends ComplexWidget implements HasText{

    public TableData() {
        setElement(Document.get().createTDElement());
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
