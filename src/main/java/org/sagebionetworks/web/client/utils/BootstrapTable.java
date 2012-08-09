package org.sagebionetworks.web.client.utils;

import java.util.List;

import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable;

public class BootstrapTable extends FlexTable {

	public BootstrapTable() {
		this.setStyleName("table table-striped table-bordered table-condensed");
	}
	
	public void setHeaders(List<List<String>> headerRows) {
		addTHeadToTable(this, headerRows);
	}	
	
	/**
	 * Initializes the table with header rows. This adds the header rows
	 * directly to the table, using the DOM-class. The current FlexTable
	 * impl doesn't have headers.
	 */
	private static void addTHeadToTable(HTMLTable table,
			List<List<String>> headerRows) {
		Element thead = DOM.createTHead();

		DOM.insertChild(table.getElement(), thead, 0);

		for (int row = 0; row < headerRows.size(); row++) {
			Element tr = DOM.createTR();
			DOM.appendChild(thead, tr);
			List<String> headerRowCols = headerRows.get(row);
			for (int col = 0; col < headerRowCols.size(); col++) {

				Element th = DOM.createTH();
				DOM.appendChild(tr, th);

				// set header text
				DOM.setInnerText(th, headerRowCols.get(col));
			}
		}

		// move the colgroup tag to the bottom so that bootstrap table renders properly (still cuts the rounding off the bottom though)
		Element el = table.getElement();
		Node colgroups = el.getChild(1); 
		el.removeChild(colgroups);
		el.appendChild(colgroups);
		
	}

}
