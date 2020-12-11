package org.sagebionetworks.web.client.widget.entity.tabs;

import java.util.function.Consumer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.evaluation.AdministerEvaluationsList;
import org.sagebionetworks.web.client.widget.evaluation.ChallengeWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorReactComponentPage;

public class ChallengeTab implements ChallengeTabView.Presenter {
	Tab tab;
	ChallengeTabView view;
	AdministerEvaluationsList evaluationList;
	ChallengeWidget challengeWidget;
	PortalGinInjector ginInjector;
	AuthenticationController authenticationController;
	GlobalApplicationState globalApplicationState;
	@Inject
	public ChallengeTab(Tab tab, PortalGinInjector ginInjector, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState) {
		this.tab = tab;
		this.ginInjector = ginInjector;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		tab.configure("Challenge", "Challenges are open science, collaborative competitions for evaluating and comparing computational algorithms or solutions to problems.", "http://sagebionetworks.org/platforms/", EntityArea.CHALLENGE);
	}

	public void lazyInject() {
		if (view == null) {
			this.view = ginInjector.getChallengeTabView();
			this.evaluationList = ginInjector.getAdministerEvaluationsList();
			this.challengeWidget = ginInjector.getChallengeWidget();
			view.setEvaluationList(evaluationList.asWidget());
			view.setChallengeWidget(challengeWidget.asWidget());
			tab.setContent(view.asWidget());
			view.setActionMenu(tab.getEntityActionMenu());
		}
	}

	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}

	public void configure(String entityId, String entityName, EntityBundle projectBundle) {
		lazyInject();
		tab.setEntityNameAndPlace(entityName, new Synapse(entityId, null, EntityArea.CHALLENGE, null));
		challengeWidget.configure(entityId, entityName);

		//This is currently only used in the "alpha" test mode where a React component is using
		// a different Evaluation editor
		Consumer<String> editEvaluationCallback = (String evaluationId) ->{
			EvaluationEditorReactComponentPage evaluationEditor = ginInjector.createEvaluationEditorReactComponentPage();
			evaluationEditor.configure(evaluationId,
					authenticationController.getCurrentUserSessionToken(),
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
		};
		evaluationList.configure(entityId, editEvaluationCallback);

		tab.configureEntityActionController(projectBundle, true, null);
	}

	public Tab asTab() {
		return tab;
	}
}
