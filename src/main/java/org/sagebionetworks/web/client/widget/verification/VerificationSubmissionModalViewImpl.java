package org.sagebionetworks.web.client.widget.verification;

import static org.sagebionetworks.web.shared.WebConstants.ACT_PROFILE_VALIDATION_REJECTION_REASONS_TABLE_PROPERTY_KEY;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.verification.VerificationState;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.RejectProfileValidationRequestModalProps;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableHeader;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.ReactComponent;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactory;

public class VerificationSubmissionModalViewImpl
  implements VerificationSubmissionWidgetView {

  public interface Binder
    extends UiBinder<Widget, VerificationSubmissionModalViewImpl> {}

  VerificationSubmissionWidgetView.Presenter presenter;

  Widget widget;

  @UiField
  Div publicallyVisible;

  @UiField
  TextBox firstName;

  @UiField
  TextBox lastName;

  @UiField
  TextBox currentAffiliation;

  @UiField
  TextBox location;

  @UiField
  Anchor orcIdAnchor;

  @UiField
  Div actOnly;

  @UiField
  Div emailAddresses;

  @UiField
  Panel filesContainer;

  @UiField
  Button submitButton;

  @UiField
  Button approveButton;

  @UiField
  Button rejectButton;

  @UiField
  Button suspendButton;

  @UiField
  Button cancelButton;

  @UiField
  Button closeButton;

  @UiField
  Button deleteButton;

  @UiField
  Button okButton;

  @UiField
  Button recreateSubmissionButton;

  @UiField
  Modal dialog;

  @UiField
  Div synAlertContainer;

  @UiField
  Div promptModalContainer;

  @UiField
  Heading modalTitle;

  @UiField
  Alert reasonAlert;

  @UiField
  Paragraph reasonAlertText;

  @UiField
  Div actStateHistoryContainer;

  @UiField
  FormGroup uploadedFilesUI;

  @UiField
  Table actStateHistoryTable;

  TableRow actStateHistoryTableHeaderRow;

  private final CellFactory cellFactory;
  private final SynapseProperties synapseProperties;
  private final SynapseReactClientFullContextPropsProvider propsProvider;

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Inject
  public VerificationSubmissionModalViewImpl(
    Binder binder,
    CellFactory cellFactory,
    SynapseProperties synapseProperties,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    widget = binder.createAndBindUi(this);
    this.cellFactory = cellFactory;
    this.synapseProperties = synapseProperties;
    this.propsProvider = propsProvider;

    // click handlers
    submitButton.addClickHandler(event -> {
      presenter.submitVerification();
    });
    approveButton.addClickHandler(event -> {
      presenter.approveVerification();
    });
    rejectButton.addClickHandler(event -> {
      presenter.rejectVerification();
    });
    suspendButton.addClickHandler(event -> {
      presenter.suspendVerification();
    });
    deleteButton.addClickHandler(event -> {
      presenter.deleteVerification();
    });
    cancelButton.addClickHandler(event -> {
      dialog.hide();
    });
    recreateSubmissionButton.addClickHandler(event -> {
      presenter.recreateVerification();
    });

    actStateHistoryTableHeaderRow = new TableRow();
    actStateHistoryTableHeaderRow.add(getTableHeader("Date"));
    actStateHistoryTableHeaderRow.add(getTableHeader("User"));
    actStateHistoryTableHeaderRow.add(getTableHeader("State"));
    actStateHistoryTableHeaderRow.add(getTableHeader("Reason"));
    actStateHistoryTableHeaderRow.add(getTableHeader("ACT Internal Notes"));
  }

  @Override
  public void showLoading() {
    // TODO Auto-generated method stub
  }

  @Override
  public void showInfo(String message) {
    DisplayUtils.showInfo(message);
  }

  @Override
  public void showErrorMessage(String message) {
    DisplayUtils.showErrorMessage(message);
  }

  @Override
  public void clear() {
    firstName.setText("");
    lastName.setText("");
    currentAffiliation.setText("");
    location.setText("");
    orcIdAnchor.setText("");
    emailAddresses.clear();
    cancelButton.setVisible(false);
    submitButton.setVisible(false);
    okButton.setVisible(false);
    approveButton.setVisible(false);
    rejectButton.setVisible(false);
    suspendButton.setVisible(false);
    deleteButton.setVisible(false);
    closeButton.setVisible(false);
    reasonAlert.setVisible(false);
    recreateSubmissionButton.setVisible(false);
    modalTitle.setText("");
    actStateHistoryTable.clear();
  }

  @Override
  public void show() {
    dialog.show();
  }

  @Override
  public void hide() {
    dialog.hide();
  }

  @Override
  public void setSynAlert(Widget w) {
    synAlertContainer.clear();
    synAlertContainer.add(w);
  }

  @Override
  public void setFileHandleList(Widget w) {
    filesContainer.clear();
    filesContainer.add(w);
  }

  @Override
  public void setTitle(String title) {
    modalTitle.setText(title);
  }

  @Override
  public void setEmails(List<String> emails) {
    emailAddresses.clear();
    for (String email : emails) {
      Paragraph p = new Paragraph();
      p.setText(email);
      emailAddresses.add(p);
    }
  }

  @Override
  public void setFirstName(String fname) {
    firstName.setValue(fname);
  }

  @Override
  public void setLastName(String lname) {
    lastName.setValue(lname);
  }

  @Override
  public void setOrganization(String organization) {
    currentAffiliation.setValue(organization);
  }

  @Override
  public void setLocation(String l) {
    location.setValue(l);
  }

  @Override
  public void setOrcID(String href) {
    orcIdAnchor.setText(href);
    orcIdAnchor.setHref(href);
  }

  @Override
  public void setSubmitButtonVisible(boolean visible) {
    submitButton.setVisible(visible);
  }

  @Override
  public void setCancelButtonVisible(boolean visible) {
    cancelButton.setVisible(visible);
  }

  @Override
  public void setOKButtonVisible(boolean visible) {
    okButton.setVisible(visible);
  }

  @Override
  public void setApproveButtonVisible(boolean visible) {
    approveButton.setVisible(visible);
  }

  @Override
  public void setRejectButtonVisible(boolean visible) {
    rejectButton.setVisible(visible);
  }

  @Override
  public void setSuspendButtonVisible(boolean visible) {
    suspendButton.setVisible(visible);
  }

  @Override
  public void setResubmitButtonVisible(boolean visible) {
    recreateSubmissionButton.setVisible(visible);
  }

  @Override
  public void setSuspendedAlertVisible(boolean visible) {
    reasonAlert.setVisible(visible);
  }

  @Override
  public void setSuspendedReason(String reason) {
    reasonAlertText.setText(reason);
  }

  @Override
  public void popupError(String message) {
    DisplayUtils.showErrorMessage(message);
  }

  @Override
  public void openWindow(String url) {
    DisplayUtils.newWindow(url, "_self", "");
  }

  @Override
  public void setPromptModal(Widget w) {
    promptModalContainer.clear();
    promptModalContainer.add(w);
  }

  @Override
  public void setDeleteButtonVisible(boolean visible) {
    deleteButton.setVisible(visible);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setProfileLink(String profileId, String href) {
    // Not used in this view implementation
  }

  @Override
  public void setState(VerificationStateEnum state) {
    // Not used in this view implementation
  }

  @Override
  public void setProfileFieldsEditable(boolean editable) {
    firstName.setEnabled(editable);
    lastName.setEnabled(editable);
    currentAffiliation.setEnabled(editable);
    location.setEnabled(editable);
  }

  @Override
  public void showRejectModal(
    String submissionId,
    VerificationStateEnum currentState,
    RejectProfileValidationRequestModalProps.Callback onRejectSuccess
  ) {
    final ReactComponent promptModalV2 = new ReactComponent();
    RejectProfileValidationRequestModalProps props =
      RejectProfileValidationRequestModalProps.create(
        submissionId,
        currentState,
        synapseProperties.getSynapseProperty(
          ACT_PROFILE_VALIDATION_REJECTION_REASONS_TABLE_PROPERTY_KEY
        ),
        true,
        onRejectSuccess,
        promptModalV2::clear
      );
    promptModalV2.render(
      React.createElementWithSynapseContext(
        SRC.SynapseComponents.RejectProfileValidationRequestModal,
        props,
        propsProvider.getJsInteropContextProps()
      )
    );
  }

  @Override
  public String getFirstName() {
    return firstName.getValue();
  }

  @Override
  public String getLastName() {
    return lastName.getValue();
  }

  @Override
  public String getLocation() {
    return location.getValue();
  }

  @Override
  public String getOrganization() {
    return currentAffiliation.getValue();
  }

  @Override
  public void setCloseButtonVisible(boolean visible) {
    closeButton.setVisible(visible);
  }

  @Override
  public void setACTStateHistoryVisible(boolean visible) {
    actStateHistoryContainer.setVisible(visible);
  }

  private TableHeader getTableHeader(String text) {
    TableHeader th = new TableHeader();
    th.setText(text);
    return th;
  }

  private TableData getNewTableData(IsWidget child) {
    TableData td = new TableData();
    td.add(child);
    return td;
  }

  private Text getTextWidget(String value) {
    String v = value == null ? "" : value;
    return new Text(v);
  }

  @Override
  public void setACTStateHistory(List<VerificationState> stateHistory) {
    actStateHistoryTable.clear();
    actStateHistoryTable.add(actStateHistoryTableHeaderRow);

    for (VerificationState state : stateHistory) {
      TableRow row = new TableRow();
      // date, user, state, reason, notes
      Cell dateRenderer = cellFactory.createRenderer(ColumnType.DATE);
      dateRenderer.setValue(state.getCreatedOn().getTime() + "");
      row.add(getNewTableData(dateRenderer));
      Cell userRenderer = cellFactory.createRenderer(ColumnType.USERID);
      userRenderer.setValue(state.getCreatedBy());
      row.add(getNewTableData(userRenderer));
      row.add(getNewTableData(getTextWidget(state.getState().toString())));
      row.add(getNewTableData(getTextWidget(state.getReason())));
      row.add(getNewTableData(getTextWidget(state.getNotes())));
      actStateHistoryTable.add(row);
    }
  }

  @Override
  public void setShowSubmissionInModalButtonVisible(boolean visible) {
    // does nothing in this implementation
  }

  @Override
  public void setUploadedFilesUIVisible(boolean visible) {
    uploadedFilesUI.setVisible(visible);
  }
}
