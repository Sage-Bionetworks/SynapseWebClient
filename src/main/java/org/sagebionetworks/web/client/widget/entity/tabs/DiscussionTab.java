package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.inject.Inject;

public class DiscussionTab implements DiscussionTabView.Presenter{
	Tab tab;
	DiscussionTabView view;
	CookieProvider cookies;

	@Inject
	public DiscussionTab(
			DiscussionTabView view,
			Tab tab,
			CookieProvider cookies
			) {
		this.view = view;
		this.tab = tab;
		this.cookies = cookies;
		tab.configure("Discussion", view.asWidget());
	}

	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}

	public void configure(String entityId, String entityName) {
		tab.setEntityNameAndPlace(entityName, new Synapse(entityId, null, EntityArea.DISCUSSION, null));
		tab.setTabListItemVisible(DisplayUtils.isInTestWebsite(cookies));
	}

	public Tab asTab(){
		return tab;
	}
}
