package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.inject.Inject;

public class DiscussionTab implements DiscussionTabView.Presenter{
	Tab tab;
	DiscussionTabView view;

	@Inject
	public DiscussionTab(
			DiscussionTabView view,
			Tab tab
			) {
		this.view = view;
		this.tab = tab;
		tab.configure("Discussion", view.asWidget());
	}

	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}

	public void configure(String entityId, String entityName) {
		tab.setEntityNameAndPlace(entityName, new Synapse(entityId, null, EntityArea.DISCUSSION, null));
		tab.setTabListItemVisible(false);
	}

	public Tab asTab(){
		return tab;
	}
}
