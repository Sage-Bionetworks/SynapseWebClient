package org.sagebionetworks.web.unitclient.widget.table.v2;

import org.sagebionetworks.web.client.widget.table.v2.ColumnModelTableRowViewer;

/**
 * Stub for ColumnModelTableRowViewer
 * @author jmhill
 *
 */
public class ColumnModelTableRowViewerStub extends ColumnModelTableRowStub implements ColumnModelTableRowViewer {

	boolean isSelectable;
	
	@Override
	public void setSelectable(boolean isSelectable) {
		this.isSelectable = isSelectable;
	}


}
