package org.sagebionetworks.web.client.widget.evaluation;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.evaluation.model.Evaluation;

public interface SubmissionViewScopeEditorView extends IsWidget {
  void setPresenter(Presenter presenter);
  void addRow(Evaluation ev);
  void clearRows();
  void add(IsWidget w);

  public interface Presenter {
    void onDeleteClicked(Evaluation evaluation);
    void onAddClicked();
  }
}
