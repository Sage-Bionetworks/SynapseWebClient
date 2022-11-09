package org.sagebionetworks.web.client.widget.table.modal.fileview;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * View shows the first step of the wizard
 *
 * @author Jay
 *
 */
public interface EntityViewScopeWidgetView extends IsWidget {
  void setVisible(boolean visible);

  void setEntityListWidget(IsWidget w);

  void setEditableEntityListWidget(IsWidget w);

  void setSynAlert(IsWidget w);

  void showModal();

  void hideModal();

  void setEditMaskAndScopeButtonVisible(boolean visible);

  void setEditMaskVisible(boolean visible);

  void setLoading(boolean loading);

  boolean isFileSelected();

  void setIsFileSelected(boolean selected);

  boolean isTableSelected();

  void setIsTableSelected(boolean selected);

  boolean isFolderSelected();

  void setIsFolderSelected(boolean selected);

  boolean isDatasetSelected();

  void setIsDatasetSelected(boolean selected);

  public interface Presenter {
    void onSave();

    void onEditScopeAndMask();

    void updateViewTypeMask();
  }

  void setPresenter(Presenter presenter);
}
