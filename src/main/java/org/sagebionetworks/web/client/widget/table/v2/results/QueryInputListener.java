package org.sagebionetworks.web.client.widget.table.v2.results;

/**
 * Abstraction for a listener to input queries.
 *
 * @author John
 *
 */
public interface QueryInputListener {
  /**
   * Called when the user explicitly execute a query.
   *
   * @param sql
   */
  public void onExecuteQuery(String sql);

  /**
   * Called when the user selects the edit button.
   */
  public void onEditResults();

  void onAddToDownloadList();
}
