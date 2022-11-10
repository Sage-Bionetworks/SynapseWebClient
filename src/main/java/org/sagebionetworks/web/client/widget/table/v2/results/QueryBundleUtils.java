package org.sagebionetworks.web.client.widget.table.v2.results;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import java.util.List;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.SelectColumn;
import org.sagebionetworks.repo.model.table.Table;

/**
 * Utilities for extracting date from QueryResultBundles
 *
 * @author John
 *
 */
public class QueryBundleUtils {

  public static final String SELECT_FROM = "SELECT * FROM ";
  public static final long DEFAULT_OFFSET = 0L;
  public static final long DEFAULT_LIMIT = 25;

  private static final String QUERY_TABLE_ID_REG_EX =
    "from[\\s]+(syn[0-9]+)[.]?([0-9]*)";
  private static final RegExp TABLE_ID_PATTERN = RegExp.compile(
    QUERY_TABLE_ID_REG_EX
  );

  public static Query getDefaultQuery(Table table) {
    return getDefaultQuery(
      table.getId(),
      table.getIsLatestVersion(),
      table.getVersionNumber()
    );
  }

  public static Query getDefaultQuery(
    String tableId,
    boolean isCurrentVersion,
    Long versionNumber
  ) {
    StringBuilder builder = new StringBuilder();
    builder.append(SELECT_FROM);
    builder.append(tableId);
    if (!isCurrentVersion) {
      builder.append("." + versionNumber);
    }
    Query query = new Query();
    query.setIncludeEntityEtag(true);
    query.setSql(builder.toString());
    query.setOffset(DEFAULT_OFFSET);
    query.setLimit(DEFAULT_LIMIT);
    return query;
  }

  /**
   * Find the select columns in the bundle.
   *
   * @return Null if any parts are null.
   */
  public static List<SelectColumn> getSelectFromBundle(
    QueryResultBundle bundle
  ) {
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
   *
   * @param query
   * @return
   */
  public static String getTableId(Query query) {
    if (query == null) {
      return null;
    }
    return getTableIdFromSql(query.getSql());
  }

  /**
   * Get the table ID from a query string
   *
   * @param query
   * @return
   */
  public static String getTableIdFromSql(String query) {
    if (query == null) {
      return null;
    }
    // Find the 'from syn123.23'
    MatchResult matcher = TABLE_ID_PATTERN.exec(query.toLowerCase());
    if (matcher != null && matcher.getGroupCount() > 1) {
      // group 1 is the synapse ID
      return matcher.getGroup(1);
    }
    return null;
  }

  /**
   * Get the table entity version from a query string (if set).
   *
   * @param query
   * @return
   */
  public static Long getTableVersion(String query) {
    if (query == null) {
      return null;
    }
    // Find the 'from syn123'
    MatchResult matcher = TABLE_ID_PATTERN.exec(query.toLowerCase());
    if (matcher != null && matcher.getGroupCount() > 2) {
      String versionNumberString = matcher.getGroup(2);
      try {
        return Long.parseLong(versionNumberString);
      } catch (NumberFormatException e) {
        // invalid version or not defined
        return null;
      }
    }
    return null;
  }

  /**
   * Get the rowset from the bundle
   *
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
