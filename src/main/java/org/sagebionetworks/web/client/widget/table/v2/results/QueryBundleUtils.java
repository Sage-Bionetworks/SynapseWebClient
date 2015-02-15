package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.List;

import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.SelectColumn;

/**
 * Utilities for extracting date from QueryResultBundles
 * 
 * @author John
 * 
 */
public class QueryBundleUtils {

	/**
	 * Find the select columns in the bundle.
	 * 
	 * @return Null if any parts are null.
	 */
	public static List<SelectColumn> getSelectFromBundle(
			QueryResultBundle bundle) {
		RowSet set = getRowSet(bundle);
		if (set != null) {
			return set.getHeaders();
		}
		return null;
	}

	/**
	 * Find the tableId in the given bundle.
	 * 
	 * @param bundle
	 * @return Null if any parts are null.
	 */
	public static String getTableId(QueryResultBundle bundle) {
		RowSet set = getRowSet(bundle);
		if (set != null) {
			return set.getTableId();
		}
		return null;
	}

	/**
	 * Get the rowset from the bundle
	 * @param bundle
	 * @return
	 */
	public static RowSet getRowSet(QueryResultBundle bundle) {
		if (bundle != null) {
			QueryResult qr = bundle.getQueryResult();
			if (qr != null) {
				return qr.getQueryResults();
			}
		}
		return null;
	}
}
