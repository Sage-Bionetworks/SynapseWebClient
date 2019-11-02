package org.sagebionetworks.web.client.view.bootstrap.table;

import org.gwtbootstrap3.client.ui.base.ComplexWidget;
import com.google.gwt.dom.client.Document;

/**
 * Simple
 * <tr>
 * 
 * @author jmhill
 *
 */
public class TableRow extends ComplexWidget {

	public TableRow() {
		setElement(Document.get().createTRElement());
	}
}
