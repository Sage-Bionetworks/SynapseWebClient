package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.user.client.ui.IsWidget;

public interface LockAccessRequirementWidgetView extends IsWidget {
  void addStyleNames(String styleNames);

  void setDeleteAccessRequirementWidget(IsWidget w);

  void setTeamSubjectsWidget(IsWidget w);

  void setEntitySubjectsWidget(IsWidget entitySubjectsWidget);

  void setAccessRequirementRelatedProjectsList(
    IsWidget accessRequirementRelatedProjectsList
  );

  void setCoveredEntitiesHeadingVisible(boolean visible);

  void setAccessRequirementIDVisible(boolean visible);

  void setAccessRequirementID(String arID);
}
