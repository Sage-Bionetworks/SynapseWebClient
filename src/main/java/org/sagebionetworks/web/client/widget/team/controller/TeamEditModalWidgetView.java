package org.sagebionetworks.web.client.widget.team.controller;

import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.utils.Callback;

public interface TeamEditModalWidgetView {
  public interface Presenter {
    public Widget asWidget();

    void setRefreshCallback(Callback refreshCallback);

    void onConfirm();

    void hide();

    void onRemovePicture();
  }

  public Widget asWidget();

  public void setAlertWidget(Widget asWidget);

  void setPresenter(Presenter presenter);

  String getName();

  String getDescription();

  void setUploadWidget(Widget uploader);

  void setImageURL(String fileHandleId);

  void setDefaultIconVisible();

  void setAuthenticatedUsersCanSendMessageToTeam(boolean canSendMessage);

  boolean canAuthenticatedUsersSendMessageToTeam();

  void showInfo(String message);

  void show();

  void hide();

  void showLoading();

  void hideLoading();

  void clear();

  void configure(Team team);

  void setTeamManagerAuthRequiredOptionActive();

  void setNoAuthNeededOptionActive();

  void setLockedDownOptionActive();

  boolean getIsTeamManagerAuthRequired();

  boolean getIsNoAuthRequired();

  boolean getIsLockedDown();
}
