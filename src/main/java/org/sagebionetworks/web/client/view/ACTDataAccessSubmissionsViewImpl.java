package org.sagebionetworks.web.client.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.Well;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.header.Header;

public class ACTDataAccessSubmissionsViewImpl
  implements ACTDataAccessSubmissionsView {

  public interface ACTViewImplUiBinder
    extends UiBinder<Widget, ACTDataAccessSubmissionsViewImpl> {}

  @UiField
  DropDownMenu stateDropdownMenu;

  @UiField
  Div synAlertContainer;

  @UiField
  Div accessRequirementContainer;

  @UiField
  Div showHideAccessRequirementButtonContainer;

  @UiField
  Div tableData;

  @UiField
  Button clearStateFilter;

  @UiField
  Span currentState;

  @UiField
  Panel accessRequirementUI;

  @UiField
  Anchor createdOnColumnHeader;

  @UiField
  CheckBox certifiedCheckbox;

  @UiField
  CheckBox validatedCheckbox;

  @UiField
  CheckBox ducCheckbox;

  @UiField
  Div ducTemplateFileContainer;

  @UiField
  CheckBox irbCheckbox;

  @UiField
  CheckBox otherAttachmentsCheckbox;

  @UiField
  CheckBox intendedDataUsePublicCheckbox;

  @UiField
  CheckBox iduCheckbox;

  @UiField
  Div subjectsContainer;

  @UiField
  TextBox expirationPeriodTextbox;

  @UiField
  Well expirationDateUI;

  @UiField
  Strong expirationDate;

  @UiField
  Div accessorSelectContainer;

  @UiField
  Div currentAccessorContainer;

  @UiField
  Button clearAccessorFilter;

  private Presenter presenter;
  private Header headerWidget;

  Widget widget;

  @Inject
  public ACTDataAccessSubmissionsViewImpl(
    ACTViewImplUiBinder binder,
    Header headerWidget
  ) {
    widget = binder.createAndBindUi(this);
    this.headerWidget = headerWidget;
    headerWidget.configure();
    clearStateFilter.addClickHandler(event -> {
      presenter.onClearStateFilter();
    });
    createdOnColumnHeader.addClickHandler(event -> {
      presenter.onCreatedOnClick();
    });
    clearAccessorFilter.addClickHandler(event -> {
      presenter.onClearAccessorFilter();
    });
  }

  @Override
  public void setPresenter(final Presenter presenter) {
    this.presenter = presenter;
    headerWidget.configure();
    headerWidget.refresh();
    Window.scrollTo(0, 0); // scroll user to top of page
  }

  @Override
  public void setAccessRequirementUIVisible(boolean visible) {
    accessRequirementUI.setVisible(visible);
  }

  @Override
  public void showErrorMessage(String message) {
    DisplayUtils.showErrorMessage(message);
  }

  @Override
  public void showLoading() {}

  @Override
  public void showInfo(String message) {
    DisplayUtils.showInfo(message);
  }

  @Override
  public void clear() {}

  @Override
  public void setSelectedStateText(String state) {
    currentState.setText(state);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setStates(List<String> states) {
    stateDropdownMenu.clear();
    for (final String state : states) {
      AnchorListItem item = new AnchorListItem(state);
      item.addClickHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            presenter.onStateSelected(state);
          }
        }
      );
      stateDropdownMenu.add(item);
    }
  }

  @Override
  public void setLoadMoreContainer(IsWidget w) {
    tableData.clear();
    tableData.add(w);
  }

  @Override
  public void setSynAlert(IsWidget w) {
    synAlertContainer.clear();
    synAlertContainer.add(w);
  }

  @Override
  public void setAccessRequirementWidget(IsWidget w) {
    accessRequirementContainer.clear();
    accessRequirementContainer.add(w);
  }

  @Override
  public void setShowHideButton(IsWidget button) {
    showHideAccessRequirementButtonContainer.clear();
    showHideAccessRequirementButtonContainer.add(button);
  }

  @Override
  public void setAreOtherAttachmentsRequired(boolean value) {
    otherAttachmentsCheckbox.setValue(value);
  }

  @Override
  public void setIsCertifiedUserRequired(boolean value) {
    certifiedCheckbox.setValue(value);
  }

  @Override
  public void setIsDUCRequired(boolean value) {
    ducCheckbox.setValue(value);
  }

  @Override
  public void setIsIDURequired(boolean value) {
    iduCheckbox.setValue(value);
  }

  @Override
  public void setIsIDUPublic(boolean value) {
    intendedDataUsePublicCheckbox.setValue(value);
  }

  @Override
  public void setIsIRBApprovalRequired(boolean value) {
    irbCheckbox.setValue(value);
  }

  @Override
  public void setIsValidatedProfileRequired(boolean value) {
    validatedCheckbox.setValue(value);
  }

  @Override
  public void setSubjectsWidget(IsWidget w) {
    subjectsContainer.clear();
    subjectsContainer.add(w);
  }

  @Override
  public void setExpirationPeriod(Long value) {
    expirationPeriodTextbox.setValue(value.toString());
  }

  @Override
  public void setProjectedExpirationDate(String date) {
    expirationDate.setText(date);
  }

  @Override
  public void setProjectedExpirationDateVisible(boolean visible) {
    expirationDateUI.setVisible(visible);
  }

  @Override
  public void setAccessorPickerWidget(IsWidget w) {
    accessorSelectContainer.clear();
    accessorSelectContainer.add(w);
  }

  @Override
  public void setSelectedAccessorUserBadge(IsWidget w) {
    currentAccessorContainer.clear();
    currentAccessorContainer.add(w);
  }

  @Override
  public void setSelectedAccessorUserBadgeVisible(boolean visible) {
    currentAccessorContainer.setVisible(visible);
  }
}
