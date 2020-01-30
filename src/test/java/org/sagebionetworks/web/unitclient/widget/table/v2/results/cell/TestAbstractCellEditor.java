package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import org.sagebionetworks.web.client.widget.table.v2.results.cell.AbstractCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditorView;

/**
 * A test implementation of an abstract class to test the abstract class.
 * 
 * @author John
 *
 */
public class TestAbstractCellEditor extends AbstractCellEditor {

	public TestAbstractCellEditor(CellEditorView view) {
		super(view);
	}

	@Override
	public boolean isValid() {
		return true;
	}

}
