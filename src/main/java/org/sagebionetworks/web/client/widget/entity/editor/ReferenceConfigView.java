package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.widget.WidgetEditorView;

public interface ReferenceConfigView extends IsWidget, WidgetEditorView {
  /**
   * Set the presenter.
   *
   * @param presenter
   */
  public void setPresenter(Presenter presenter);

  public void setReference(String reference);

  public String getReference();

  /**
   * Presenter interface
   */
  public interface Presenter {}
}
