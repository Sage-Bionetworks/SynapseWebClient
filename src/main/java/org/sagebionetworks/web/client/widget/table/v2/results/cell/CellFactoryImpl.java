package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnTypeViewEnum;

/**
 * Factory for creating table cells.
 * 
 * @author John
 *
 */
public class CellFactoryImpl implements CellFactory {

	@Override
	public Cell createRenderer(ColumnTypeViewEnum type) {
		return new StringRenderer();
	}

	@Override
	public Cell createEditor(ColumnTypeViewEnum type) {
		return new StringEditor();
	}

}
