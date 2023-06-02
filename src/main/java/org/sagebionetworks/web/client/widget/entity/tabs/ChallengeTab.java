package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.inject.Inject;
import java.util.function.Consumer;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.menu.v3.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;
import org.sagebionetworks.web.client.widget.evaluation.AdministerEvaluationsList;
import org.sagebionetworks.web.client.widget.evaluation.ChallengeWidget;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorModal;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorReactComponentPage;

public class ChallengeTab implements ChallengeTabView.Presenter {

  Tab tab;
  ChallengeTabView view;
  AdministerEvaluationsList evaluationList;
  ChallengeWidget challengeWidget;
  PortalGinInjector ginInjector;
  AuthenticationController authenticationController;
  GlobalApplicationState globalApplicationState;
  CookieProvider cookies;
  EntityActionMenu actionMenuWidget;

  String entityId;
  EvaluationEditorModal evalEditor;
  UserEntityPermissions permissions;

  @Inject
  public ChallengeTab(
    Tab tab,
    PortalGinInjector ginInjector,
    AuthenticationController authenticationController,
    GlobalApplicationState globalApplicationState,
    CookieProvider cookies
  ) {
    this.tab = tab;
    this.ginInjector = ginInjector;
    this.authenticationController = authenticationController;
    this.globalApplicationState = globalApplicationState;
    this.cookies = cookies;
    tab.configure(
      "Challenge",
      "challenge",
      "Challenges are open science, collaborative competitions for evaluating and comparing computational algorithms or solutions to problems.",
      "http://sagebionetworks.org/platforms/",
      EntityArea.CHALLENGE
    );
    tab.configureOrientationBanner(
      "Challenges",
      "Getting Started With Your Challenges",
      "Challenges are open science, collaborative competitions for evaluating and comparing computational algorithms or solutions to problems.",
      null,
      null,
      "Learn More About Challenges",
      "https://help.synapse.org/docs/Challenges.1985184148.html"
    );
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

    //This is currently only used in the "alpha" test mode where a React component is using
    // a different Evaluation editor
    Consumer<String> editEvaluationCallback = (String evaluationId) -> {
      showEvaluationEditor(null, evaluationId);
    };

    evaluationList.configure(entityId, editEvaluationCallback);

    tab.configureEntityActionController(projectBundle, true, null, null);
  }

  /**
   * Set only one of entityId or evaluationId to be non-null
   * @param entityId non-null if creating new evaluation
   * @param evaluationId non-null if updating existing evaluation
   */
  private void showEvaluationEditor(String entityId, String evaluationId) {
    //This is currently only used in the "alpha" test mode where a React component is using
    // a different Evaluation editor

    EvaluationEditorReactComponentPage evaluationEditor = ginInjector.createEvaluationEditorReactComponentPage();
    globalApplicationState.setIsEditing(true);
    evaluationEditor.configure(
      evaluationId,
      entityId,
      authenticationController.getCurrentUserAccessToken(),
      globalApplicationState.isShowingUTCTime(),
      // onPageBack() callback
      () -> {
        evaluationEditor.removeFromParent();
        view.showAdminTabContents();
        evaluationList.refresh();
        globalApplicationState.setIsEditing(false);
      }
    );
    view.hideAdminTabContents();
    view.addEvaluationEditor(evaluationEditor);
  }

  @Override
  public void showCreateNewEvaluationEditor(String entityId) {
    //This is currently only used in the "alpha" test mode where a React component is using
    // a different Evaluation editor

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
