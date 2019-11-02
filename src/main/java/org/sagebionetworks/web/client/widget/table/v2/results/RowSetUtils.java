package org.sagebionetworks.web.client.widget.table.v2.results;

import static org.sagebionetworks.web.client.StringUtils.emptyAsNull;
import static org.sagebionetworks.web.client.StringUtils.isValueChanged;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.PartialRow;
import org.sagebionetworks.repo.model.table.PartialRowSet;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowSet;

/**
 * Functional utilities for the complex operations on RowSets.
 * 
 * @author John
 * 
 */
public class RowSetUtils {

	public static final String ETAG_COLUMN_NAME = "etag";


	/**
	 * Given an original RowSet and an updated RowSet, build a delta composed of the changes. This can
	 * include new rows, deleted rows and updated rows.
	 * 
	 * @param original
	 * @param updatedHeaders
	 * @param updateRows
	 * @return
	 */
	public static PartialRowSet buildDelta(RowSet original, List<Row> updateRows, List<ColumnModel> selectData) {
		List<PartialRow> delta = new LinkedList<PartialRow>();
		// Map the all rows with IDs
		LinkedHashMap<Long, Row> originalRowMap = buildRowIdToRowMap(original.getRows());
		LinkedHashMap<Long, Row> updateRowMap = buildRowIdToRowMap(updateRows);
		// Delete are any row from the original that is no longer in the update
		Iterator<Long> it = originalRowMap.keySet().iterator();
		while (it.hasNext()) {
			Long rowId = it.next();
			if (!updateRowMap.containsKey(rowId)) {
				// Delete this row by adding PartialRow with an ID but no map.
				PartialRow pr = new PartialRow();
				pr.setRowId(rowId);
				delta.add(pr);
			}
		}
		// Updates are rows in the updated with an ID. Creates are rows with no
		// rowId.
		for (Row toUpdate : updateRows) {
			PartialRow pr = null;
			if (toUpdate.getRowId() != null) {
				// update
				// Lookup this row in the original
				Row originalRow = originalRowMap.get(toUpdate.getRowId());
				if (originalRow == null) {
					throw new IllegalArgumentException("An updated row has a RowID: " + toUpdate.getRowId() + " that does not match any row from the original results set.");
				}
				// Build the delta
				pr = buildPartialRow(selectData, toUpdate, originalRow);
				// map the changes
			} else {
				// create
				if (toUpdate.getValues() != null) {
					pr = buildPartialRow(selectData, toUpdate, null);
				}
			}
			if (pr != null) {
				delta.add(pr);
			}
		}
		PartialRowSet prs = new PartialRowSet();
		prs.setRows(delta);
		prs.setTableId(original.getTableId());
		return prs;
	}

	/**
	 * Build a PartialRow from the headers, updated row, and original row.
	 * 
	 * @param headers The ColumnIds that map to the values in the row to update.
	 * @param toUpdate The row that contains the changes to apply. The update can be either a create
	 *        (null rowId) or update.
	 * @param original If not null the values will be compared with the update row and any changes
	 *        applied to the resulting PartialRow. If null a new PartialRow will be created with all of
	 *        the values from the updated.
	 * @return A new PartialRow will be returned if the
	 */
	public static PartialRow buildPartialRow(List<ColumnModel> headers, Row toUpdate, Row original) {
		if (toUpdate.getValues() == null) {
			return null;
		}
		if (toUpdate.getValues().size() != headers.size()) {
			throw new IllegalArgumentException("Row.values.size() does not match row.headers.size()");
		}
		HashMap<String, String> map = new HashMap<String, String>(toUpdate.getValues().size());
		for (int i = 0; i < toUpdate.getValues().size(); i++) {
			ColumnModel header = headers.get(i);
			// aggregate rows can have null headers so skip them.
			if (header != null && header.getId() != null) {
				String updateValue = toUpdate.getValues().get(i);
				if (original == null) {
					map.put(header.getId(), emptyAsNull(updateValue));
				} else {
					if (isValueChanged(original.getValues().get(i), updateValue)) {
						map.put(header.getId(), emptyAsNull(updateValue));
					}
				}
			}
		}
		if (map.isEmpty()) {
			// There was no change
			return null;
		} else {
			PartialRow pr = new PartialRow();
			if (original != null) {
				pr.setRowId(original.getRowId());
				pr.setEtag(original.getEtag());
			}
			pr.setValues(map);
			return pr;
		}
	}


	/**
	 * Build up a map of rowIds to rows. Any row without a RowID will no be included in the Map.
	 * 
	 * @param rows
	 * @return
	 */
	public static LinkedHashMap<Long, Row> buildRowIdToRowMap(List<Row> rows) {
		LinkedHashMap<Long, Row> results = new LinkedHashMap<Long, Row>();
		if (rows != null) {
			for (Row row : rows) {
				if (row.getRowId() != null) {
					results.put(row.getRowId(), row);
				}
			}
		}
		return results;
	}
}
