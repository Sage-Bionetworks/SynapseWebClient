package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.evaluation.AdministerEvaluationsList;
import org.sagebionetworks.web.client.widget.evaluation.ChallengeWidget;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.inject.Inject;

public class ChallengeTab implements ChallengeTabView.Presenter{
	Tab tab;
	ChallengeTabView view;
	AdministerEvaluationsList evaluationList;
	ChallengeWidget challengeWidget;
	PortalGinInjector ginInjector;
	@Inject
	public ChallengeTab(Tab tab, PortalGinInjector ginInjector){
		this.tab = tab;
		this.ginInjector = ginInjector;
		tab.configure("Challenge", "Challenges are computational contests organized through the Dream Challenges.", "http://dreamchallenges.org");
		tab.setTabListItemVisible(false);
	}
	
	public void lazyInject() {
		if (view == null) {
			this.view = ginInjector.getChallengeTabView();
			this.evaluationList = ginInjector.getAdministerEvaluationsList();
			this.challengeWidget = ginInjector.getChallengeWidget();
			view.setEvaluationList(evaluationList.asWidget());
			view.setChallengeWidget(challengeWidget.asWidget());
			tab.setContent(view.asWidget());
		}
	}
	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}
	
	public void configure(String entityId, String entityName) {
		lazyInject();
		tab.setEntityNameAndPlace(entityName, new Synapse(entityId, null, EntityArea.ADMIN, null));
		tab.setTabListItemVisible(false);
		Callback isChallengeOrEvaluationQueueCallback = new Callback() {
			@Override
			public void invoke() {
				tab.setTabListItemVisible(true);
			}
		};
		challengeWidget.configure(entityId, isChallengeOrEvaluationQueueCallback);
		evaluationList.configure(entityId, isChallengeOrEvaluationQueueCallback);
	}
	
	public Tab asTab(){
		return tab;
	}
}
