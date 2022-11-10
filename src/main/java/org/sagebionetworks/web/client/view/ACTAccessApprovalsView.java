package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.ArrayList;
import java.util.Date;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroup;
import org.sagebionetworks.web.client.SynapseView;

public interface ACTAccessApprovalsView extends IsWidget, SynapseView {
  void setPresenter(Presenter presenter);

  void setLoadMoreContainer(IsWidget w);

  void setSynAlert(IsWidget w);

  void setShowHideButton(IsWidget button);

  void setAccessRequirementWidget(IsWidget w);

  void setAccessRequirementUIVisible(boolean visible);

  void setSubmitterPickerWidget(IsWidget w);

  void setAccessorPickerWidget(IsWidget w);

  void setSelectedSubmitterUserBadge(IsWidget w);

  void setSelectedSubmitterUserBadgeVisible(boolean visible);

  void setSelectedAccessorUserBadge(IsWidget w);

  void setSelectedAccessorUserBadgeVisible(boolean visible);

  void setExpiresBeforeDate(Date date);

  void setClearAccessRequirementFilterButtonVisible(boolean visible);

  void resetExportButton();

  void export(ArrayList<AccessorGroup> exportData);

  public interface Presenter {
    void onClearSubmitterFilter();

    void onClearAccessorFilter();

    void onClearExpireBeforeFilter();

    void onClearAccessRequirementFilter();

    void onExpiresBeforeDateSelected(Date selectedDate);

    void onExportData();
  }
}
