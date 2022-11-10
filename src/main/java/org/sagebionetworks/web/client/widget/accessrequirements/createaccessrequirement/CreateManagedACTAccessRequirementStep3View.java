package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * View shows the third step of the wizard for ACT AR
 *
 * @author Jay
 *
 */
public interface CreateManagedACTAccessRequirementStep3View extends IsWidget {
  void setPresenter(Presenter p);
  void setReviewerUIVisible(boolean visible);
  void setReviewerBadge(IsWidget w);
  void setReviewerSearchBox(IsWidget w);

  /*
   * Presenter interface
   */
  public interface Presenter {
    void onRemoveReviewer();
  }
}
