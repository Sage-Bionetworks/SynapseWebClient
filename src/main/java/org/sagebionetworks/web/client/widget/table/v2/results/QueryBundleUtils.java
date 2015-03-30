package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.List;

import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.SelectColumn;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * Utilities for extracting date from QueryResultBundles
 * 
 * @author John
 * 
 */
public class QueryBundleUtils {

	private static final String SYN = "syn";
	private static final String QUERY_TABLE_ID_REG_EX = "from[\\s]+syn[0-9]+";
	private static final RegExp TABLE_ID_PATTERN = RegExp.compile(QUERY_TABLE_ID_REG_EX);

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
	 * Find a tableId from the SQL of the given query.
	 * @param query
	 * @return
	 */
	public static String getTableId(Query query){
		if(query == null){
			return null;
		}
		return getTableIdFromSql(query.getSql());
	}
	/**
	 * Get the table ID from a query string
	 * @param query
	 * @return
	 */
	public static String getTableIdFromSql(String query){
		if(query == null){
			return null;
		}
		// Find the 'from syn123'
		MatchResult matcher = TABLE_ID_PATTERN.exec(query.toLowerCase());
		if(matcher != null){
			String fromGroup = matcher.getGroup(0);
			// keep only the syn123
			return fromGroup.substring(fromGroup.indexOf(SYN), fromGroup.length());
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
