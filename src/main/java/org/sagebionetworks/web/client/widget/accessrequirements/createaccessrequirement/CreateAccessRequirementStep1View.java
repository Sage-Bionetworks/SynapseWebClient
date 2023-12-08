package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.EntitySubjectsWidget;

/**
 * View shows the first step of the wizard
 *
 * @author Jay
 *
 */
public interface CreateAccessRequirementStep1View extends IsWidget {
  void setTeamSubjects(IsWidget w);

  boolean isManagedACTAccessRequirementType();

  boolean isACTAccessRequirementType();

  boolean isTermsOfUseAccessRequirementType();

  void setPresenter(Presenter p);

  String getTeamIds();

  void setTeamIdsString(String ids);

  void setAccessRequirementTypeSelectionVisible(boolean visible);

  void setName(String name);
  String getName();

  /**
   * Presenter interface
   */
  public interface Presenter {
    void onAddTeams();
  }

  void setEntitySubjects(IsWidget entitySubjectsWidget);

  void showEntityUI();
}
