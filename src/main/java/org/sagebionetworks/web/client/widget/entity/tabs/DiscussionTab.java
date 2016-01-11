package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.discussion.ThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.modal.NewThreadModal;

import com.google.inject.Inject;

public class DiscussionTab implements DiscussionTabView.Presenter{
	private final static Long PROJECT_VERSION_NUMBER = null;

	Tab tab;
	DiscussionTabView view;
	CookieProvider cookies;
	// TODO: use this token to navigate between threads within the discussion tab
	String areaToken = null;
	NewThreadModal newThreadModal;

	private ThreadListWidget discussionListWidget;

	@Inject
	public DiscussionTab(
			DiscussionTabView view,
			Tab tab,
			ThreadListWidget discussionListWidget,
			NewThreadModal newThreadModal,
			CookieProvider cookies
			) {
		this.view = view;
		this.tab = tab;
		this.discussionListWidget = discussionListWidget;
		this.newThreadModal = newThreadModal;
		this.cookies = cookies;
		tab.configure("Discussion", view.asWidget());
		view.setPresenter(this);
		view.setDiscussionList(discussionListWidget.asWidget());
		view.setNewThreadModal(newThreadModal.asWidget());
	}

	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}

	public void configure(String entityId, String entityName) {
		tab.setEntityNameAndPlace(entityName, new Synapse(entityId, PROJECT_VERSION_NUMBER, EntityArea.DISCUSSION, areaToken));
		tab.setTabListItemVisible(DisplayUtils.isInTestWebsite(cookies));
	}

	public Tab asTab(){
		return tab;
	}

	@Override
	public void onClickNewThread() {
		newThreadModal.show();
	}
}
