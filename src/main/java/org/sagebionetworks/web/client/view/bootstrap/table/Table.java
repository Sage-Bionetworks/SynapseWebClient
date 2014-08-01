package org.sagebionetworks.web.client.view.bootstrap.table;

import org.gwtbootstrap3.client.ui.base.ComplexWidget;
import org.gwtbootstrap3.client.ui.constants.Styles;

import com.google.gwt.dom.client.Document;

/**
 * Simple <table class="table">
 * 
 * @author jmhill
 *
 */
public class Table extends ComplexWidget {

    public Table() {
    	setElement(Document.get().createTableElement());
    }
}
