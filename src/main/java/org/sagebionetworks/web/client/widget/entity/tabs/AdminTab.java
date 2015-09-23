package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.AdministerEvaluationsList;

import com.google.inject.Inject;

public class AdminTab implements AdminTabView.Presenter{
	Tab tab;
	AdminTabView view;
	AdministerEvaluationsList evaluationList;
	
	@Inject
	public AdminTab(
			AdminTabView view,
			Tab tab,
			AdministerEvaluationsList evaluationList
			) {
		this.view = view;
		this.tab = tab;
		this.evaluationList = evaluationList;
		
		view.setEvaluationList(evaluationList.asWidget());
		tab.configure("Admin", view.asWidget());
	}
	
	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.setTabClickedCallback(onClickCallback);
	}
	
	public void configure(String entityId) {
		tab.setPlace(new Synapse(entityId, null, EntityArea.ADMIN, null));
		tab.getTabListItem().setVisible(false);
		evaluationList.configure(entityId, new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean isVisible) {
				tab.getTabListItem().setVisible(isVisible);
			}
		});
	}
	
	public Tab asTab(){
		return tab;
	}
}
