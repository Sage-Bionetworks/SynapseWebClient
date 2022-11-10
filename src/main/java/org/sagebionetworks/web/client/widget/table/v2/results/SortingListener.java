package org.sagebionetworks.web.client.widget.table.v2.results;

/**
 * Listener for sorting events.
 *
 * @author jhill
 *
 */
public interface SortingListener {
  /**
   * Called when the header is clicked.
   */
  public void onToggleSort(String header);
}
