package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.evaluation.AdministerEvaluationsList;
import org.sagebionetworks.web.client.widget.evaluation.ChallengeWidget;
import com.google.inject.Inject;

public class ChallengeTab implements ChallengeTabView.Presenter {
	Tab tab;
	ChallengeTabView view;
	AdministerEvaluationsList evaluationList;
	ChallengeWidget challengeWidget;
	PortalGinInjector ginInjector;
	@Inject
	public ChallengeTab(Tab tab, PortalGinInjector ginInjector) {
		this.tab = tab;
		this.ginInjector = ginInjector;
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
		challengeWidget.configure(entityId);
		evaluationList.configure(entityId);
		tab.configureEntityActionController(projectBundle, true, null);
	}

	public Tab asTab() {
		return tab;
	}
}
