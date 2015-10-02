package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.AdministerEvaluationsList;

import com.google.inject.Inject;

public class ChallengeTab implements ChallengeTabView.Presenter{
	Tab tab;
	ChallengeTabView view;
	AdministerEvaluationsList evaluationList;
	
	@Inject
	public ChallengeTab(
			ChallengeTabView view,
			Tab tab,
			AdministerEvaluationsList evaluationList
			) {
		this.view = view;
		this.tab = tab;
		this.evaluationList = evaluationList;
		
		view.setEvaluationList(evaluationList.asWidget());
		tab.configure("Challenge", view.asWidget());
	}
	
	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}
	
	public void configure(String entityId) {
		tab.setPlace(new Synapse(entityId, null, EntityArea.ADMIN, null));
		tab.setTabListItemVisible(false);
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
