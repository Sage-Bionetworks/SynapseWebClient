package org.sagebionetworks.web.client.view.bootstrap.table;

import org.gwtbootstrap3.client.ui.base.ComplexWidget;
import com.google.gwt.dom.client.Document;

/**
 * Simple <tbody>
 * 
 * @author jmhill
 *
 */
public class TBody extends ComplexWidget {

	public TBody() {
		setElement(Document.get().createTBodyElement());
	}
}
