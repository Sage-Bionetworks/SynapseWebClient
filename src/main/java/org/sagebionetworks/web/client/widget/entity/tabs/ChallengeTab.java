package org.sagebionetworks.web.client.widget.entity.tabs;

import java.util.function.Consumer;
import javax.inject.Inject;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.menu.v3.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;
import org.sagebionetworks.web.client.widget.evaluation.AdministerEvaluationsList;
import org.sagebionetworks.web.client.widget.evaluation.ChallengeWidget;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorReactComponentPage;

public class ChallengeTab implements ChallengeTabView.Presenter {

  Tab tab;
  ChallengeTabView view;
  AdministerEvaluationsList evaluationList;
  ChallengeWidget challengeWidget;
  PortalGinInjector ginInjector;
  AuthenticationController authenticationController;
  GlobalApplicationState globalApplicationState;
  PopupUtilsView popupUtils;
  EntityActionMenu actionMenuWidget;

  String entityId;
  UserEntityPermissions permissions;

  @Inject
  public ChallengeTab(
    Tab tab,
    PortalGinInjector ginInjector,
    AuthenticationController authenticationController,
    GlobalApplicationState globalApplicationState,
    PopupUtilsView popupUtils
  ) {
    this.tab = tab;
    this.ginInjector = ginInjector;
    this.authenticationController = authenticationController;
    this.globalApplicationState = globalApplicationState;
    this.popupUtils = popupUtils;
    tab.configure("Challenge", EntityArea.CHALLENGE);
  }

  public void lazyInject() {
    if (view == null) {
      this.view = ginInjector.getChallengeTabView();
      this.evaluationList = ginInjector.getAdministerEvaluationsList();
      this.challengeWidget = ginInjector.getChallengeWidget();
      this.actionMenuWidget = tab.getEntityActionMenu();
      view.setEvaluationList(evaluationList.asWidget());
      view.setChallengeWidget(challengeWidget.asWidget());
      tab.setContent(view.asWidget());
      view.setActionMenu(actionMenuWidget);
    }
  }

  public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
    tab.addTabClickedCallback(onClickCallback);
  }

  public void configure(
    String entityId,
    String entityName,
    EntityBundle projectBundle
  ) {
    lazyInject();
    this.entityId = entityId;
    this.permissions = projectBundle.getPermissions();

    tab.setEntityNameAndPlace(
      entityName,
      new Synapse(entityId, null, EntityArea.CHALLENGE, null)
    );
    challengeWidget.configure(entityId, entityName);

    Consumer<String> editEvaluationCallback = (String evaluationId) ->
      showEvaluationEditor(null, evaluationId);

    evaluationList.configure(entityId, editEvaluationCallback);

    tab.configureEntityActionController(projectBundle, true, null, null);
  }

  /**
   * Set only one of entityId or evaluationId to be non-null
   * @param entityId non-null if creating new evaluation
   * @param evaluationId non-null if updating existing evaluation
   */
  private void showEvaluationEditor(String entityId, String evaluationId) {
    EvaluationEditorReactComponentPage evaluationEditor =
      ginInjector.createEvaluationEditorReactComponentPage();
    Runnable doNavigateBack = () -> {
      evaluationEditor.removeFromParent();
      view.showAdminTabContents();
      evaluationList.refresh();
    };
    evaluationEditor.configure(
      evaluationId,
      entityId,
      authenticationController.getCurrentUserAccessToken(),
      globalApplicationState.isShowingUTCTime(),
      // onPageBack() callback
      () -> {
        if (globalApplicationState.isEditing()) {
          popupUtils.showConfirmDialog(
            "",
            DisplayConstants.NAVIGATE_AWAY_CONFIRMATION_MESSAGE,
            () -> {
              globalApplicationState.setIsEditing(false);
              doNavigateBack.run();
            }
          );
        } else {
          doNavigateBack.run();
        }
      }
    );
    view.hideAdminTabContents();
    view.addEvaluationEditor(evaluationEditor);
  }

  @Override
  public void showCreateNewEvaluationEditor(String entityId) {
    showEvaluationEditor(entityId, null);
  }

  public void updateActionMenuCommands() {
    actionMenuWidget.setActionVisible(
      Action.ADD_EVALUATION_QUEUE,
      permissions.getCanEdit()
    );
    actionMenuWidget.setActionListener(
      Action.ADD_EVALUATION_QUEUE,
      (action, e) -> showCreateNewEvaluationEditor(entityId)
    );
  }

  public Tab asTab() {
    return tab;
  }
}
