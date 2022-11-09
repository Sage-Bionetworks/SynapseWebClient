package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.widget.WidgetEditorView;

public interface PreviewConfigView extends IsWidget, WidgetEditorView {
  /**
   * Set the presenter.
   *
   * @param presenter
   */
  void setPresenter(Presenter presenter);

  void setEntityId(String entityId);

  String getEntityId();

  void setVersion(String version);

  String getVersion();

  /**
   * Presenter interface
   */
  public interface Presenter {
    void onEntityFinderButtonClicked();
  }
}
