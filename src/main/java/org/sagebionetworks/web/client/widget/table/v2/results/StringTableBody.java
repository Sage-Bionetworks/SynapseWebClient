package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnTypeViewEnum;

/**
 * A bootstrap table body that can contain rows that vary by column type.
 * @author jmhill
 *
 */
public class StringTableBody extends TBody  {

	public StringTableBody(CellFactory factory,  Map<Integer, ColumnTypeViewEnum> types, List<List<String>> rows){
		super();
		// Add each row
		for(List<String> row: rows){
			this.add(new StringTableRow(factory, types, row, false));
		}
	}
}
