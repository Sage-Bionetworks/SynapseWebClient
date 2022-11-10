package org.sagebionetworks.web.client.widget.table.v2;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a view that includes a query text box and execution button.
 *
 * @author John
 *
 */
public interface QueryInputView extends IsWidget {
  /**
   * Business logic for this widget.
   */
  public interface Presenter {
    /**
     * Called when the users presses the execute query button.
     */
    void onExecuteQuery();

    /**
     * Called to rest the query.
     */
    void onReset();
  }

  /**
   * Bind this view to its presenter.
   *
   * @param presenter
   */
  public void setPresenter(Presenter presenter);

  /**
   * Set an accepted and validated query string.
   *
   * @param startQuery
   */
  void setInputQueryString(String startQuery);

  /**
   *
   * @param loading
   */
  void setQueryInputLoading(boolean loading);

  /**
   * The the SQL string from the input box.
   *
   * @return
   */
  public String getInputQueryString();

  /**
   * Show or hide the input error message.
   *
   * @param b
   */
  public void showInputError(boolean visible);

  /**
   * Set the error message.
   *
   * @param string
   */
  public void setInputErrorMessage(String string);

  public void setQueryInputVisible(boolean visible);
}
