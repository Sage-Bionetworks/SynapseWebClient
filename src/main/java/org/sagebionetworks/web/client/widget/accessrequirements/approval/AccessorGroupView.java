package org.sagebionetworks.web.client.widget.accessrequirements.approval;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.List;
import org.sagebionetworks.repo.model.dataaccess.AccessApprovalNotification;

public interface AccessorGroupView extends IsWidget {
  /**
   * Set the presenter.
   *
   * @param presenter
   */
  void setPresenter(Presenter presenter);

  void addStyleNames(String styleNames);

  void setVisible(boolean visible);

  void setSynAlert(IsWidget w);

  void clearAccessors();

  void addAccessor(IsWidget w);

  void setSubmittedBy(IsWidget w);

  void setExpiresOn(String expiresOn);

  void showAccessRequirementDialog(IsWidget w);

  void clearEmails();

  void addEmail(String username);
  void showNotifications(List<AccessApprovalNotification> notifications);

  /**
   * Presenter interface
   */
  public interface Presenter {
    void onShowAccessRequirement();

    void onRevoke();

    void onShowNotifications();
  }
}
