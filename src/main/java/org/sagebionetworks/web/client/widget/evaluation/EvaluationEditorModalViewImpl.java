package org.sagebionetworks.web.client.widget.evaluation;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.Date;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormControlStatic;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.DateTimePicker;
import org.sagebionetworks.web.client.widget.NumberBox;
import org.sagebionetworks.web.client.widget.user.UserBadge;

public class EvaluationEditorModalViewImpl
  implements EvaluationEditorModalView {

  @UiField
  Modal modal;

  @UiField
  TextBox nameField;

  @UiField
  TextBox submissionInstructionsField;

  @UiField
  TextBox submissionReceiptField;

  @UiField
  TextBox descriptionField;

  @UiField
  FormControlStatic createdOnDiv;

  @UiField
  Div createdByDiv;

  @UiField
  NumberBox submissionLimitField;

  @UiField
  NumberBox numberOfRoundsField;

  @UiField
  NumberBox roundDurationDays;

  @UiField
  NumberBox roundDurationHours;

  @UiField
  NumberBox roundDurationMinutes;

  @UiField
  NumberBox roundDurationSeconds;

  @UiField
  DateTimePicker roundStartPicker;

  @UiField
  Div synAlertContainer;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  Widget widget;

  private Presenter presenter;

  public interface Binder
    extends UiBinder<Widget, EvaluationEditorModalViewImpl> {}

  private static Binder uiBinder = GWT.create(Binder.class);

  @Inject
  public EvaluationEditorModalViewImpl(UserBadge createdByBadge) {
    widget = uiBinder.createAndBindUi(this);
    saveButton.addClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          presenter.onSave();
        }
      }
    );
    cancelButton.addClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          modal.hide();
        }
      }
    );
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public String getEvaluationName() {
    return nameField.getText();
  }

  @Override
  public void setEvaluationName(String name) {
    nameField.setText(name);
  }

  @Override
  public String getSubmissionInstructionsMessage() {
    return submissionInstructionsField.getText();
  }

  @Override
  public void setSubmissionInstructionsMessage(String message) {
    submissionInstructionsField.setText(message);
  }

  @Override
  public String getSubmissionReceiptMessage() {
    return submissionReceiptField.getText();
  }

  @Override
  public void setSubmissionReceiptMessage(String message) {
    submissionReceiptField.setText(message);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void show() {
    modal.show();
  }

  @Override
  public void hide() {
    modal.hide();
  }

  @Override
  public void setSynAlert(IsWidget w) {
    synAlertContainer.clear();
    synAlertContainer.add(w);
  }

  @Override
  public void setCreatedByWidget(IsWidget w) {
    createdByDiv.clear();
    createdByDiv.add(w);
  }

  @Override
  public void setDescription(String description) {
    descriptionField.setText(description);
  }

  @Override
  public String getDescription() {
    return descriptionField.getText();
  }

  @Override
  public void setCreatedOn(String createdOnString) {
    createdOnDiv.setText(createdOnString);
  }

  @Override
  public void clear() {
    nameField.setValue("");
    descriptionField.setValue("");
    submissionInstructionsField.setValue("");
    submissionReceiptField.setValue("");
    submissionLimitField.setValue(null);
    numberOfRoundsField.setValue(null);
    roundDurationDays.setValue(null);
    roundDurationHours.setValue(null);
    roundDurationMinutes.setValue(null);
    roundDurationSeconds.setValue(null);
    roundStartPicker.setValue(null);
  }

  @Override
  public void setNumberOfRounds(Long numberOfRounds) {
    numberOfRoundsField.setValue(numberOfRounds.toString());
  }

  @Override
  public Double getNumberOfRounds() {
    return numberOfRoundsField.getNumberValue();
  }

  @Override
  public Long getRoundDuration() {
    DurationHelper duration = new DurationHelper(
      roundDurationSeconds.getNumberValue(),
      roundDurationMinutes.getNumberValue(),
      roundDurationHours.getNumberValue(),
      roundDurationDays.getNumberValue()
    );
    return duration.getDurationMs();
  }

  @Override
  public Date getRoundStart() {
    return roundStartPicker.getValue();
  }

  @Override
  public Double getSubmissionLimit() {
    return submissionLimitField.getNumberValue();
  }

  @Override
  public void setRoundDuration(Long roundDurationMs) {
    if (roundDurationMs != null) {
      DurationHelper duration = new DurationHelper(roundDurationMs);
      roundDurationSeconds.setValue(duration.getSeconds().toString());
      roundDurationMinutes.setValue(duration.getMinutes().toString());
      roundDurationHours.setValue(duration.getHours().toString());
      roundDurationDays.setValue(duration.getDays().toString());
    }
  }

  @Override
  public void setRoundStart(Date roundStart) {
    roundStartPicker.setValue(roundStart);
  }

  @Override
  public void setSubmissionLimit(Long submissionLimit) {
    submissionLimitField.setValue(submissionLimit.toString());
  }
}
