package org.sagebionetworks.web.client.widget.evaluation;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ChallengeWidgetView extends IsWidget {
  void setChallengeNameHeading(String challengeName);

  /**
   * Set the presenter.
   *
   * @param presenter
   */
  void setPresenter(Presenter presenter);

  void setChallengeTeamWidget(Widget w);

  void setChallengeVisible(boolean visible);

  void setChallengeId(String challengeId);

  void add(Widget w);

  void setSelectTeamModal(Widget w);

  public interface Presenter {
    void onEditTeamClicked();
  }
}
