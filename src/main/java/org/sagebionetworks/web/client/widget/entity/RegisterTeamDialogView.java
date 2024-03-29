package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.List;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.ShowsErrors;
import org.sagebionetworks.web.client.utils.Callback;

public interface RegisterTeamDialogView extends IsWidget, ShowsErrors {
  /**
   * Set the presenter.
   *
   * @param presenter
   */
  void setPresenter(Presenter presenter);

  void setRecruitmentMessage(String message);

  String getRecruitmentMessage();

  void setTeams(List<Team> teams);

  void showModal();

  void hideModal();

  void setNoTeamsFoundVisible(boolean isVisible);

  void showConfirmDialog(String message, Callback okCallback);

  void showInfo(String message);

  void setNewTeamLink(String url);

  /**
   * Presenter interface
   */
  public interface Presenter {
    void teamSelected(int selectedIndex);

    void onOk();

    void refreshRegistratableTeams();
  }
}
