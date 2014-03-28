package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.table.Row;

public class TableUtils {

	public static TableModel convertRowToModel(List<String> headers, Row row) {
		if(headers == null || row == null) return null;		
		String id = row.getRowId() != null ? row.getRowId().toString() : null;		
		TableModel model = new TableModel(id);
		for(int i=0; i<headers.size(); i++) {				        		
			model.put(headers.get(i), row.getValues().get(i));
		}
		return model;
	}
	
	public static Row convertModelToRow(List<String> headers, TableModel model) {
		if(headers == null || model == null) return null;		
		Row row = new Row();
		Long id = null;
		try {		
			id = model.getId() != null ? Long.parseLong(model.getId()) : null;
		} catch (NumberFormatException e) { /* temp TableModel id */ }
		row.setRowId(id);
		List<String> values = new ArrayList<String>();		
		for(String col : headers) {
			values.add(model.get(col));
		}
		row.setValues(values);
		return row;
	}

}
