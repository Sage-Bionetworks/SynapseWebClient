package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.widget.WidgetEditorView;

public interface SynapseFormConfigView extends IsWidget, WidgetEditorView {
  void setPresenter(Presenter presenter);

  void setEntityId(String entityId);

  String getEntityId();

  public interface Presenter {
    void onEntityFinderButtonClicked();
  }
}
