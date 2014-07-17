package org.sagebionetworks.web.client.view.bootstrap.table;

import org.gwtbootstrap3.client.ui.base.ComplexWidget;

import com.google.gwt.dom.client.Document;

/**
 * Simple <th>
 * 
 * @author jmhill
 *
 */
public class TableHeader extends ComplexWidget {

    public TableHeader() {
        setElement(Document.get().createTHElement());
    }
}