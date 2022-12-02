package org.sagebionetworks.web.client.widget.entity.browse;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

public class FilesBrowser implements SynapseWidgetPresenter {

  private final FilesBrowserView view;
  String entityId;

  @Inject
  public FilesBrowser(FilesBrowserView view) {
    this.view = view;
  }

  /**
   * Configure tree view with given entityId's children as start set
   *
   * @param entityId
   */
  public void configure(String entityId) {
    this.entityId = entityId;
    view.clear();
    view.configure(entityId);
  }

  public void clear() {
    view.clear();
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  public void setEntityClickedHandler(CallbackP<String> callback) {
    view.setEntityClickedHandler(callback);
  }

  public void setActionMenu(IsWidget w) {
    view.setActionMenu(w);
  }

  public void setAddToDownloadListWidget(IsWidget w) {
    view.setAddToDownloadListWidget(w);
  }
}
