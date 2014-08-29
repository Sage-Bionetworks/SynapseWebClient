package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;

import com.google.gwt.user.client.ui.Widget;

/**
 * Simple cell stub.
 * 
 * @author John
 *
 */
public class CellStub implements Cell {
	
	private String value;

	@Override
	public Widget asWidget() {
		return null;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	}

}
