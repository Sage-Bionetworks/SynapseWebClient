package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.List;
import org.sagebionetworks.web.client.SynapseView;

public interface ACTDataAccessSubmissionsView extends IsWidget, SynapseView {
  void setPresenter(Presenter presenter);

  void setLoadMoreContainer(IsWidget w);

  void setStates(List<String> states);

  void setSynAlert(IsWidget w);

  void setSelectedStateText(String state);

  void setShowHideButton(IsWidget button);

  void setAccessRequirementWidget(IsWidget w);

  void setAreOtherAttachmentsRequired(boolean value);

  void setExpirationPeriod(Long value);

  void setIsCertifiedUserRequired(boolean value);

  void setIsDUCRequired(boolean value);

  void setIsIDURequired(boolean value);

  void setIsIDUPublic(boolean value);

  void setIsIRBApprovalRequired(boolean value);

  void setIsValidatedProfileRequired(boolean value);

  void setSubjectsWidget(IsWidget w);

  void setAccessRequirementUIVisible(boolean visible);

  void setProjectedExpirationDateVisible(boolean visible);

  void setProjectedExpirationDate(String date);

  void setAccessorPickerWidget(IsWidget accessorSuggestWidget);

  void setSelectedAccessorUserBadge(IsWidget selectedAccessorUserBadge);

  void setSelectedAccessorUserBadgeVisible(boolean b);

  public interface Presenter {
    void onClearStateFilter();

    void onStateSelected(String state);

    void onCreatedOnClick();

    void onClearAccessorFilter();
  }
}
