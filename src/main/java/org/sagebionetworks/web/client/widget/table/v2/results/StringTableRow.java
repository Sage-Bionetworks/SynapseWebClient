package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnTypeViewEnum;

/**
 * A table row that contains either editors or renderers where the types of columns can vary.
 * @author jmhill
 *
 */
public class StringTableRow extends TableRow {
	
	/**
	 * Build a new row 
	 * @param factory
	 * @param types
	 * @param cells
	 * @param isEditor
	 */
	public StringTableRow(CellFactory factory, Map<Integer, ColumnTypeViewEnum> types, List<String> cells, boolean isEditor){
		super();
		// Add each cell to this row
		for(int i=0; i<cells.size(); i++){
			String value = cells.get(i);
			ColumnTypeViewEnum type = types.get(i);
			if(isEditor){
				this.add(factory.createEditor(type, value));
			}else{
				this.add(factory.createRenderer(type, value));
			}
		}
	}

}
