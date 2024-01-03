package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.LockAccessRequirement;
import org.sagebionetworks.web.client.utils.Callback;

public class LockAccessRequirementWidget implements IsWidget {

  private LockAccessRequirementWidgetView view;
  LockAccessRequirement ar;
  DeleteAccessRequirementButton deleteAccessRequirementButton;
  TeamSubjectsWidget teamSubjectsWidget;
  EntitySubjectsWidget entitySubjectsWidget;
  AccessRequirementRelatedProjectsList accessRequirementRelatedProjectsList;

  @Inject
  public LockAccessRequirementWidget(
    LockAccessRequirementWidgetView view,
    TeamSubjectsWidget teamSubjectsWidget,
    EntitySubjectsWidget entitySubjectsWidget,
    AccessRequirementRelatedProjectsList accessRequirementRelatedProjectsList,
    DeleteAccessRequirementButton deleteAccessRequirementButton
  ) {
    this.view = view;
    this.teamSubjectsWidget = teamSubjectsWidget;
    this.entitySubjectsWidget = entitySubjectsWidget;
    this.deleteAccessRequirementButton = deleteAccessRequirementButton;
    view.setDeleteAccessRequirementWidget(deleteAccessRequirementButton);
    view.setTeamSubjectsWidget(teamSubjectsWidget);
    view.setEntitySubjectsWidget(entitySubjectsWidget);
    view.setAccessRequirementRelatedProjectsList(
      accessRequirementRelatedProjectsList
    );
  }

  public void setRequirement(
    LockAccessRequirement ar,
    Callback refreshCallback
  ) {
    this.ar = ar;
    deleteAccessRequirementButton.configure(ar, refreshCallback);
    teamSubjectsWidget.configure(ar.getSubjectIds());
    entitySubjectsWidget.configure(ar.getSubjectIds());
    accessRequirementRelatedProjectsList.configure(ar.getId().toString());
  }

  public void addStyleNames(String styleNames) {
    view.addStyleNames(styleNames);
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }
}
