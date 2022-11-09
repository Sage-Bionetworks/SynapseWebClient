package org.sagebionetworks.web.client.widget.evaluation;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.jsinterop.EvaluationCardProps;
import org.sagebionetworks.web.client.jsinterop.EvaluationJSObject;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidget;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationRowWidget.EvaluationActionHandler;
import org.sagebionetworks.web.client.widget.sharing.EvaluationAccessControlListModalWidget;
import org.sagebionetworks.web.shared.WidgetConstants;

public class AdministerEvaluationsList
  implements SynapseWidgetPresenter, EvaluationActionHandler {

  private ChallengeClientAsync challengeClient;
  private GlobalApplicationState globalApplicationState;
  private AdministerEvaluationsListView view;
  private EvaluationAccessControlListModalWidget aclEditor;
  private SynapseAlert synAlert;
  private String entityId;
  private EvaluationEditorModal evalEditor;
  private AuthenticationController authenticationController;
  private SubmitToEvaluationWidget submitToEvaluationWidget;

  //This is currently only used in the "alpha" test mode for using React components to perform the Evaluation edit
  private Consumer<String> onEditEvaluation;

  @Inject
  public AdministerEvaluationsList(
    AdministerEvaluationsListView view,
    ChallengeClientAsync challengeClient,
    EvaluationAccessControlListModalWidget aclEditor,
    EvaluationEditorModal evalEditor,
    SynapseAlert synAlert,
    GlobalApplicationState globalApplicationState,
    AuthenticationController authenticationController,
    SubmitToEvaluationWidget submitToEvaluationWidget
  ) {
    this.challengeClient = challengeClient;
    this.globalApplicationState = globalApplicationState;
    this.authenticationController = authenticationController;
    this.submitToEvaluationWidget = submitToEvaluationWidget;
    fixServiceEntryPoint(challengeClient);
    this.aclEditor = aclEditor;
    this.view = view;
    this.synAlert = synAlert;
    this.evalEditor = evalEditor;
    view.add(evalEditor);
    view.add(aclEditor);
    view.setPresenter(this);
    view.add(synAlert);
  }

  /**
   *
   * @param evaluations List of evaluations to display
   * @param evaluationCallback call back with the evaluation if it is selected
   */
  public void configure(String entityId, Consumer<String> onEditEvaluation) {
    this.entityId = entityId;
    this.onEditEvaluation = onEditEvaluation;
    view.clearRows();
    synAlert.clear();
    boolean timeInUtc = globalApplicationState.isShowingUTCTime();
    String accessToken = authenticationController.getCurrentUserAccessToken();

    challengeClient.getSharableEvaluations(
      entityId,
      new AsyncCallback<List<Evaluation>>() {
        @Override
        public void onSuccess(List<Evaluation> evaluations) {
          for (Evaluation evaluation : evaluations) {
            if (evaluation.getQuota() == null) {
              createEvaluationCardReactComponent(
                evaluation,
                timeInUtc,
                accessToken,
                onEditEvaluation
              );
            } else {
              view.addRow(evaluation);
            }
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          synAlert.handleException(caught);
        }
      }
    );
  }

  private void createEvaluationCardReactComponent(
    Evaluation evaluation,
    boolean timeInUtc,
    String accessToken,
    Consumer<String> onEditEvaluation
  ) {
    EvaluationCardProps.Callback onEdit = () ->
      onEditEvaluation.accept(evaluation.getId());
    EvaluationCardProps.Callback onModifyAccess = () ->
      onShareClicked(evaluation);
    EvaluationCardProps.Callback onSubmit = () -> {
      HashMap<String, String> submitToEvaluationParams = new HashMap<>();
      submitToEvaluationParams.put(
        WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY,
        evaluation.getId()
      );
      submitToEvaluationParams.put(WidgetConstants.BUTTON_TEXT_KEY, "Submit");

      // The submitToEvaluation widget has a button, which we choose not to show since the React component also has a button, so we simulate the button click immediately
      submitToEvaluationWidget.configure(
        null,
        submitToEvaluationParams,
        null,
        null
      );
      submitToEvaluationWidget.submitToChallengeClicked();
    };
    EvaluationCardProps.Callback onDeleteSuccess = this::refresh;

    EvaluationCardProps props = EvaluationCardProps.create(
      EvaluationJSObject.fromEvaluation(evaluation),
      onEdit,
      onModifyAccess,
      onSubmit,
      onDeleteSuccess
    );

    view.addReactComponent(evaluation, props);
  }

  @Override
  public void refresh() {
    configure(entityId, onEditEvaluation);
  }

  @Override
  public void onEditClicked(Evaluation evaluation) {
    // configure and show modal for editing evaluation
    evalEditor.configure(
      evaluation,
      () -> {
        refresh();
      }
    );
    evalEditor.show();
  }

  @Override
  public void onShareClicked(Evaluation evaluation) {
    aclEditor.configure(evaluation, null);
    aclEditor.show();
  }

  @Override
  public void onDeleteClicked(Evaluation evaluation) {
    challengeClient.deleteEvaluation(
      evaluation.getId(),
      new AsyncCallback<Void>() {
        @Override
        public void onSuccess(Void result) {
          refresh();
        }

        @Override
        public void onFailure(Throwable caught) {
          synAlert.handleException(caught);
        }
      }
    );
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }
}
