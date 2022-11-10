package org.sagebionetworks.web.client.widget.entity.act;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;

public interface ApproveUserAccessModalView extends IsWidget {
  void setPresenter(Presenter presenter);

  void setSynAlert(Widget asWidget);

  void setAccessRequirementIDs(List<String> states);

  void setUserPickerWidget(Widget w);

  void setLoadingEmailWidget(Widget w);

  String getAccessRequirement();

  String getEmailMessage();

  Widget getEmailBodyWidget(String html);

  void setAccessRequirement(String num);

  void setApproveProcessing(boolean processing);

  void setRevokeProcessing(boolean processing);

  void setDatasetTitle(String text);

  void setMessageBody(String html);

  void setMessageEditArea(String html);

  void startLoadingEmail();

  void finishLoadingEmail();

  void showInfo(String message);

  void setLoadingEmailVisible(boolean visible);

  void show();

  void hide();

  /**
   * Presenter interface
   */
  public interface Presenter {
    void onSubmit();

    void onAccessRequirementIDSelected(String state);

    void onRevoke();
  }

  void setAccessRequirementWidget(AccessRequirementWidget arWidget);
}
