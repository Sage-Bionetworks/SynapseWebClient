package org.sagebionetworks.web.client.widget.table.modal.fileview;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * View shows the first step of the wizard
 *
 * @author Jay
 *
 */
public interface CreateTableViewWizardStep1View extends IsWidget {
  /**
   * Name of entity chosen by the user
   *
   */
  String getName();

  void setName(String name);

  String getDescription();

  void setDescription(String description);

  /**
   * Add widget to set/get scope.
   */
  void setEntityViewScopeWidget(IsWidget scopeWidget);
  void setSubmissionViewScopeWidget(IsWidget scopeWidget);

  void setEntityViewScopeWidgetVisible(boolean visible);
  void setSubmissionViewScopeWidgetVisible(boolean visible);

  void setViewTypeOptionsVisible(boolean visible);

  boolean isFileSelected();

  void setIsFileSelected(boolean selected);

  boolean isTableSelected();

  void setIsTableSelected(boolean selected);

  boolean isFolderSelected();

  void setIsFolderSelected(boolean selected);

  boolean isDatasetSelected();

  void setIsDatasetSelected(boolean selected);

  public interface Presenter {
    void updateViewTypeMask();
  }

  void setPresenter(Presenter presenter);
}
