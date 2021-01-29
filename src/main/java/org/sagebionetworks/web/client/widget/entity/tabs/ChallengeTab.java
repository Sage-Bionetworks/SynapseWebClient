package org.sagebionetworks.web.client.widget.entity.tabs;

import java.util.function.Consumer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.evaluation.AdministerEvaluationsList;
import org.sagebionetworks.web.client.widget.evaluation.ChallengeWidget;
import com.google.inject.Inject;
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
	ActionMenuWidget actionMenuWidget;

	String entityId;
	EvaluationEditorModal evalEditor;
	UserEntityPermissions permissions;

	@Inject
	public ChallengeTab(Tab tab, PortalGinInjector ginInjector, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState, CookieProvider cookies) {
		this.tab = tab;
		this.ginInjector = ginInjector;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.cookies = cookies;
		tab.configure("Challenge", "Challenges are open science, collaborative competitions for evaluating and comparing computational algorithms or solutions to problems.", "http://sagebionetworks.org/platforms/", EntityArea.CHALLENGE);
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

	public void configure(String entityId, String entityName, EntityBundle projectBundle) {
		lazyInject();
		this.entityId = entityId;
		this.permissions = projectBundle.getPermissions();

		tab.setEntityNameAndPlace(entityName, new Synapse(entityId, null, EntityArea.CHALLENGE, null));
		challengeWidget.configure(entityId, entityName);

		//This is currently only used in the "alpha" test mode where a React component is using
		// a different Evaluation editor
		Consumer<String> editEvaluationCallback = (String evaluationId) ->{
			showEvaluationEditor(null, evaluationId);
		};

		evaluationList.configure(entityId, editEvaluationCallback);

		tab.configureEntityActionController(projectBundle, true, null);
	}

	/**
	 * Set only one of entityId or evaluationId to be non-null
	 * @param entityId non-null if creating new evaluation
	 * @param evaluationId non-null if updating existing evaluation
	 */
	private void showEvaluationEditor(String entityId, String evaluationId){
		//This is currently only used in the "alpha" test mode where a React component is using
		// a different Evaluation editor

		EvaluationEditorReactComponentPage evaluationEditor = ginInjector.createEvaluationEditorReactComponentPage();
		evaluationEditor.configure(evaluationId,
				entityId, authenticationController.getCurrentUserSessionToken(),
				globalApplicationState.isShowingUTCTime(),
				// onPageBack() callback
				() ->{
					evaluationEditor.removeFromParent();
					view.showAdminTabContents();
					evaluationList.refresh();
				}
		);
		view.hideAdminTabContents();
		view.addEvaluationEditor(evaluationEditor);
	}

	@Override
	public void showCreateNewEvaluationEditor(String entityId){
		//This is currently only used in the "alpha" test mode where a React component is using
		// a different Evaluation editor

		showEvaluationEditor(entityId, null);
	}


	public void updateActionMenuCommands(){
		actionMenuWidget.setActionVisible(Action.ADD_EVALUATION_QUEUE, permissions.getCanEdit());
		actionMenuWidget.setActionListener(Action.ADD_EVALUATION_QUEUE, (Action action) -> {
			showCreateNewEvaluationEditor(entityId);
		});
	}

	private void showOldAddEvaluationQueueModal() {
		getEvaluationEditorModal().configure(entityId, () -> {});
		getEvaluationEditorModal().show();
	}

	private EvaluationEditorModal getEvaluationEditorModal() {
		if (evalEditor == null) {
			evalEditor = ginInjector.getEvaluationEditorModal();
		}
		return evalEditor;
	}

	public Tab asTab() {
		return tab;
	}
}
