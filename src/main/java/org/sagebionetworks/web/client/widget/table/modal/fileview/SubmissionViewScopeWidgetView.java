package org.sagebionetworks.web.client.widget.table.modal.fileview;

import com.google.gwt.user.client.ui.IsWidget;

/**
 *
 * @author Jay
 *
 */
public interface SubmissionViewScopeWidgetView extends IsWidget {
  void setVisible(boolean visible);

  void setEvaluationListWidget(IsWidget w);

  void setSubmissionViewScopeEditor(IsWidget w);

  void setSynAlert(IsWidget w);

  void showModal();

  void hideModal();

  void setEditButtonVisible(boolean visible);

  void setLoading(boolean loading);

  public interface Presenter {
    void onSave();
    void onEdit();
  }

  void setPresenter(Presenter presenter);
}
