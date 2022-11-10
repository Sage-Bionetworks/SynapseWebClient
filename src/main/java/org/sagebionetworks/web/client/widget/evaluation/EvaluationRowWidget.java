package org.sagebionetworks.web.client.widget.evaluation;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.HashMap;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormControlStatic;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.ListItem;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Text;
import org.gwtbootstrap3.client.ui.html.UnorderedList;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.SubmissionQuota;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidget;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WidgetConstants;

/**
 * Deprecated in favor of React.createElement(SRC.SynapseComponents.EvaluationCard, EvaluationCardProps)
 */
@Deprecated
public class EvaluationRowWidget implements IsWidget {

  @UiField
  Text evaluationNameText;

  @UiField
  Button shareButton;

  @UiField
  Button editButton;

  @UiField
  Button deleteButton;

  @UiField
  FormControlStatic descriptionField;

  @UiField
  FormControlStatic submissionInstructionsField;

  @UiField
  FormControlStatic submissionReceiptField;

  @UiField
  FormControlStatic createdOnDiv;

  @UiField
  Div createdByDiv;

  @UiField
  Div submitToEvaluationContainer;

  @UiField
  Div quotaUI;

  @UiField
  FormControlStatic roundStart;

  @UiField
  FormControlStatic submissionLimitField;

  @UiField
  FormControlStatic numberOfRoundsField;

  @UiField
  FormControlStatic roundDurationField;

  Widget widget;
  UserBadge userBadge;

  public interface Binder extends UiBinder<Widget, EvaluationRowWidget> {}

  private static Binder uiBinder = GWT.create(Binder.class);
  private Evaluation evaluation;
  private EvaluationActionHandler handler;
  SubmitToEvaluationWidget submitToEvaluationButton;
  private SynapseJavascriptClient synapseClient;
  private DateTimeUtils dateTimeUtils;

  public interface EvaluationActionHandler {
    void refresh();

    void onEditClicked(Evaluation evaluation);

    void onShareClicked(Evaluation evaluation);

    void onDeleteClicked(Evaluation evaluation);
  }

  @Inject
  public EvaluationRowWidget(
    UserBadge userBadge,
    DateTimeUtils dateTimeUtils,
    SubmitToEvaluationWidget submitToEvaluationButton,
    SynapseJavascriptClient jsClient
  ) {
    this.userBadge = userBadge;
    this.dateTimeUtils = dateTimeUtils;
    this.submitToEvaluationButton = submitToEvaluationButton;
    this.synapseClient = jsClient;
    widget = uiBinder.createAndBindUi(this);
    shareButton.addClickHandler(event -> {
      handler.onShareClicked(evaluation);
    });
    editButton.addClickHandler(event -> {
      handler.onEditClicked(evaluation);
    });
    deleteButton.addClickHandler(event -> {
      DisplayUtils.showConfirmDialog(
        "Delete Evaluation Queue?",
        DisplayConstants.CONFIRM_DELETE_EVAL_QUEUE + evaluation.getName(),
        new Callback() {
          @Override
          public void invoke() {
            handler.onDeleteClicked(evaluation);
          }
        }
      );
    });
    createdByDiv.add(userBadge);
    submitToEvaluationContainer.add(submitToEvaluationButton);
  }

  public void configure(
    Evaluation evaluation,
    EvaluationActionHandler handler
  ) {
    this.evaluation = evaluation;
    this.handler = handler;
    String evaluationText =
      evaluation.getName() + " (" + evaluation.getId() + ")";
    evaluationNameText.setText(evaluationText);
    userBadge.configure(evaluation.getOwnerId());
    descriptionField.setText(evaluation.getDescription());
    submissionInstructionsField.setText(
      evaluation.getSubmissionInstructionsMessage()
    );
    submissionReceiptField.setText(evaluation.getSubmissionReceiptMessage());
    createdOnDiv.setText(
      dateTimeUtils.getDateString(evaluation.getCreatedOn())
    );
    SubmissionQuota quota = evaluation.getQuota();
    quotaUI.setVisible(quota != null);
    if (quota != null) {
      if (quota.getFirstRoundStart() != null) {
        roundStart.setText(
          dateTimeUtils.getDateString(quota.getFirstRoundStart())
        );
      }
      if (quota.getSubmissionLimit() != null) {
        submissionLimitField.setText(quota.getSubmissionLimit().toString());
      }
      if (quota.getNumberOfRounds() != null) {
        numberOfRoundsField.setText(quota.getNumberOfRounds().toString());
      }
      if (quota.getRoundDurationMillis() != null) {
        roundDurationField.setText(
          new DurationHelper(quota.getRoundDurationMillis()).toString()
        );
      }
    }
    HashMap<String, String> submitToEvaluationParams = new HashMap<>();
    String subchallengeList = evaluation.getId();
    submitToEvaluationParams.put(
      WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY,
      subchallengeList
    );
    submitToEvaluationParams.put(WidgetConstants.BUTTON_TEXT_KEY, "Submit");
    submitToEvaluationButton.configure(
      null,
      submitToEvaluationParams,
      null,
      null
    );
  }

  @UiHandler("migrateButton")
  public void migrateToEvaluationRound(ClickEvent event) {
    Modal modal = new Modal();
    modal.setRemoveOnHide(true);
    modal.setTitle("Migrate to Evaluation Round");
    ModalBody body = new ModalBody();

    body.add(
      new Paragraph(
        "Migrating will switch from the deprecated SubmissionQuota to the new Evaluation Rounds system," +
        " which will allow more control over submission limits. You will be able to enforce submission limits for multiple resetting periods (daily, weekly, monthly) during a single round instead of defining multiple rounds.<br/><br/>" +
        " Migration is <b>not always recommended</b> for <b>on-going Challenges</b> as the migration will alter the submission limit reset times:"
      )
    );
    body.add(
      new UnorderedList(
        new ListItem(
          "Round duration of 1 day will start counting submission limits at 00:00 UTC of each day"
        ),
        new ListItem(
          "Round duration of 1 week will start counting submission limits on the Monday of each calendar week"
        ),
        new ListItem(
          "Round duration of 1 month will start counting submission limits on the 1st day of each calendar month"
        )
      )
    );
    body.add(
      new Paragraph(
        "In other words, submission limits will <b>no longer be based off</b> of the <b>first round start date</b>. <br/>" +
        "<b>It is highly recommended to create a new test Evaluation and try out the new system before migrating existing Evaluations</b> <br/> <br/>"
      )
    );
    body.add(
      new Heading(
        HeadingSize.H3,
        "Are you sure you want to perform the migration? This will only affect the \"Quota\" section of your Evaluation. All other settings will remain the same."
      )
    );

    modal.add(body);
    modal.show();

    Div errorContainer = new Div();
    body.add(errorContainer);

    ModalFooter footer = new ModalFooter();
    modal.add(footer);

    Button cancelButton = new Button(
      "Cancel",
      cancelClickEvent -> {
        modal.hide();
      }
    );
    footer.add(cancelButton);

    Button migrate = new Button(
      "Migrate",
      migrateClickEvent -> {
        errorContainer.clear();
        synapseClient.migrateSubmissionQuotaToEvaluationRound(
          evaluation.getId(),
          new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
              errorContainer.add(
                new Alert(
                  "ERROR migrating evaluation quota: \n\n" +
                  throwable.getMessage(),
                  AlertType.DANGER
                )
              );
            }

            @Override
            public void onSuccess(Void unused) {
              modal.hide();
              handler.refresh();
            }
          }
        );
      }
    );
    migrate.setType(ButtonType.PRIMARY);
    footer.add(migrate);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }
}
