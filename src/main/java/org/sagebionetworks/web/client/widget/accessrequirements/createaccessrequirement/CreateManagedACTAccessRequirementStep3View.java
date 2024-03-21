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
  void configure(String accessRequirementId);
  void saveAcl();

  /*
   * Presenter interface
   */
  public interface Presenter {
    void onSaveComplete(boolean saveSuccessful);
  }
}
