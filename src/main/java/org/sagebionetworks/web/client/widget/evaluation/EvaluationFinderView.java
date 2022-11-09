package org.sagebionetworks.web.client.widget.evaluation;

import com.google.gwt.user.client.ui.IsWidget;

public interface EvaluationFinderView extends IsWidget {
  void setPresenter(Presenter presenter);

  void setSynAlert(IsWidget w);
  void setEvaluationList(IsWidget w);
  void setPaginationWidget(IsWidget w);
  void show();
  void hide();

  public interface Presenter {
    void onOk();
  }
}
