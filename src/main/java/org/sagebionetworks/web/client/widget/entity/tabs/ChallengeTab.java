package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.evaluation.AdministerEvaluationsList;
import org.sagebionetworks.web.client.widget.evaluation.ChallengeWidget;

import com.google.inject.Inject;

public class ChallengeTab implements ChallengeTabView.Presenter{
	Tab tab;
	ChallengeTabView view;
	AdministerEvaluationsList evaluationList;
	ChallengeWidget challengeWidget;
	@Inject
	public ChallengeTab(
			ChallengeTabView view,
			Tab tab,
			AdministerEvaluationsList evaluationList,
			ChallengeWidget challengeWidget
			) {
		this.view = view;
		this.tab = tab;
		this.evaluationList = evaluationList;
		this.challengeWidget = challengeWidget;
		view.setEvaluationList(evaluationList.asWidget());
		view.setChallengeWidget(challengeWidget.asWidget());
		tab.configure("Challenge", view.asWidget(), "Challenges are computational contests organized through the Dream Challenges.", "http://dreamchallenges.org");
		tab.setTabListItemVisible(false);
	}
	
	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}
	
	public void configure(String entityId, String entityName) {
		tab.setEntityNameAndPlace(entityName, new Synapse(entityId, null, EntityArea.ADMIN, null));
		tab.setTabListItemVisible(false);
		challengeWidget.configure(entityId);
		evaluationList.configure(entityId, new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean isVisible) {
				tab.setTabListItemVisible(isVisible);
			}
		});
	}
	
	public Tab asTab(){
		return tab;
	}
}
