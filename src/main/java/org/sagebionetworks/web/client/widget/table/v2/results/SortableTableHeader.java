package org.sagebionetworks.web.client.widget.table.v2.results;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.repo.model.table.SortDirection;

/**
 * Abstraction for a table header that can show sorting, and it click-able to change sorting.
 *
 * @author jhill
 *
 */
public interface SortableTableHeader extends IsWidget {
  /**
   * Configure this header before using.
   *
   * @param text
   * @param handler
   */
  public void configure(String text, SortingListener listener);

  /**
   * Set the icon to be shown with this header.
   *
   * @param icon
   */
  public void setSortDirection(SortDirection direction);

  public void setIsResizable(boolean isResizable);

  public void setMinimumWidth(String minWidth);
}
