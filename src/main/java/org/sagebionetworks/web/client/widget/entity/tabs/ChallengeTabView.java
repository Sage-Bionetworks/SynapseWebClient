package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorReactComponentPage;

public interface ChallengeTabView extends IsWidget {
  void hideAdminTabContents();

  void showAdminTabContents();

  void addEvaluationEditor(EvaluationEditorReactComponentPage evaluationEditor);

  public interface Presenter {
    void showCreateNewEvaluationEditor(String entityId);
  }

  void setChallengeWidget(Widget w);

  void setEvaluationList(Widget w);

  void setActionMenu(IsWidget w);
}
