package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.widget.WidgetEditorView;

public interface ProvenanceConfigView extends IsWidget, WidgetEditorView {
  /**
   * Set the presenter.
   *
   * @param presenter
   */
  void setPresenter(Presenter presenter);

  void setEntityList(String entityList);

  String getEntityList();

  void setDepth(String depth);

  String getDepth();

  void setProvDisplayHeight(String provDisplayHeight);

  String getProvDisplayHeight();

  void setIsExpanded(boolean b);

  boolean isExpanded();

  /**
   * Presenter interface
   */
  public interface Presenter {}
}
