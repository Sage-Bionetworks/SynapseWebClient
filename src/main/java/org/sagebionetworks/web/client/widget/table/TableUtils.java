package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
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

	/**
	 * Convert ColumnModel list to a list of ColumnModel id strings (headers)
	 * @param columns
	 * @return
	 */
	public static List<String> extractHeaders(List<ColumnModel> columns) {
		List<String> headers = new ArrayList<String>();
		if(columns != null) {
			for(ColumnModel model : columns) {
				headers.add(model.getId());
			}
		}
		return headers;
	}

	/**
	 * If a column doesn't have a proper ColumnModel id (i.e. a Long) then wrap it as a DerivedColumn
	 * @param idToCol
	 * @param resultColumnId
	 * @return
	 */
	public static ColumnModel wrapDerivedColumnIfNeeded(final Map<String, ColumnModel> idToCol, String resultColumnId) {
		// test for strait column id, otherwise it is a derived column
		try {				
			Long.parseLong(resultColumnId);
			if(idToCol.containsKey(resultColumnId)) {
				return idToCol.get(resultColumnId);
			} // ignore unknown non-derived columns
		} catch (NumberFormatException e) {									
			return createDerivedColumn(resultColumnId);				
		}
		return null;
	}

	/**
	 * Create a DerivedColumnModel
	 * @param resultColumnId
	 * @return
	 */
	public static DerivedColumnModel createDerivedColumn(String resultColumnId) {
		DerivedColumnModel derivedCol = new DerivedColumnModel();
		derivedCol.setId(resultColumnId);
		derivedCol.setName(resultColumnId);
		derivedCol.setColumnType(ColumnType.STRING);
		return derivedCol;
	}

	public static String escapeColumnName(String name) {
		String escaped = name;
		if(name.contains("\"")) 
			escaped = name.replaceAll("\"", "\"\"");		
		return "\"" + escaped + "\"";
	}	

	
}
