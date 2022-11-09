package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.web.client.SynapseView;

public interface SubmitToEvaluationWidgetView extends IsWidget, SynapseView {
  /**
   * Set the presenter.
   *
   * @param presenter
   */
  void setPresenter(Presenter presenter);

  void configure(String buttonText);

  void showUnavailable(String message);

  void showAnonymousRegistrationMessage();

  void showInfo(String message);

  void setEvaluationSubmitterWidget(Widget widget);

  /**
   * Presenter interface
   */
  public interface Presenter {
    void gotoLoginPage();

    void submitToChallengeClicked();
  }
}
