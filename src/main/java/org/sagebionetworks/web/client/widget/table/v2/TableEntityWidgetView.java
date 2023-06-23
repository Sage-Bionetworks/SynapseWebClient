package org.sagebionetworks.web.client.widget.table.v2;

import com.google.gwt.user.client.ui.IsWidget;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.jsinterop.DatasetEditorProps;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps.OnQueryCallback;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps.OnQueryResultBundleCallback;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps.OnViewSharingSettingsHandler;
import org.sagebionetworks.web.client.utils.Callback;

/**
 * Abstraction for a widget of a TableEntity.
 *
 * @author John
 *
 */
public interface TableEntityWidgetView extends IsWidget {
  /**
   * Set the presenter.
   *
   * @param presenter
   */
  void setPresenter(TableEntityWidgetView.Presenter presenter);

  /**
   * Configure the view with the table data.
   *
   * @param bundle
   * @param isEditable
   */
  void configure(EntityBundle bundle, boolean isEditable);

  /**
   *
   * @param type
   * @param message
   */
  public void showTableMessage(AlertType type, String message);

  /**
   * Show or hide the table message.
   *
   * @param visible
   */
  public void setTableMessageVisible(boolean visible);

  /**
   * Add a modal to the page.
   *
   * @param w
   */

  public void addModalWidget(IsWidget w);

  void setScopeVisible(boolean visible);

  boolean isScopeVisible();

  void setSchemaVisible(boolean visible);

  boolean isSchemaVisible();

  void showErrorMessage(String message);

  void showConfirmDialog(
    String title,
    String confirmationMessage,
    Callback yesCallback
  );

  void setAddToDownloadList(IsWidget w);

  void setItemsEditorVisible(boolean visible);

  interface Presenter {
    DatasetEditorProps getItemsEditorProps();

    /**
     * Allows the view to update the state of the collapsible schema panel and keep the copy text in the action menu in sync
     */
    void toggleSchemaCollapse();

    /**
     * Allows the view to update the state of the collapsible scope panel and keep the copy text in the action menu in sync
     */
    void toggleScopeCollapse();
  }

  void configureQueryWrapperPlotNav(
    String sql,
    String initQueryJson,
    OnQueryCallback onQueryBundleRequestChange,
    OnQueryResultBundleCallback onQueryResultBundleChange,
    OnViewSharingSettingsHandler onViewSharingSettingsHandler,
    boolean hideSqlEditorControl
  );

  void configureTableOnly(String sql);

  void setQueryWrapperPlotNavVisible(boolean visible);
}
