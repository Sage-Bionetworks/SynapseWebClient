package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.discussion.ThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.modal.NewThreadModal;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class DiscussionTab implements DiscussionTabView.Presenter{
	private final static Long PROJECT_VERSION_NUMBER = null;

	Tab tab;
	DiscussionTabView view;
	CookieProvider cookies;
	// TODO: use this token to navigate between threads within the discussion tab
	String areaToken = null;
	NewThreadModal newThreadModal;
	ThreadListWidget threadListWidget;
	SynapseAlert synAlert;
	Forum forum;
	DiscussionForumClientAsync discussionForumClient;
	CallbackP<Void> newThreadCallback = new CallbackP<Void>(){

		@Override
		public void invoke(Void param) {
			configureThreadList();
		}
	};

	@Inject
	public DiscussionTab(
			DiscussionTabView view,
			Tab tab,
			SynapseAlert synAlert,
			DiscussionForumClientAsync discussionForumClient,
			ThreadListWidget threadListWidget,
			NewThreadModal newThreadModal,
			CookieProvider cookies
			) {
		this.view = view;
		this.tab = tab;
		this.synAlert = synAlert;
		this.threadListWidget = threadListWidget;
		this.newThreadModal = newThreadModal;
		this.discussionForumClient = discussionForumClient;
		this.cookies = cookies;
		tab.configure("Discussion", view.asWidget());
		view.setPresenter(this);
		view.setThreadList(threadListWidget.asWidget());
		view.setNewThreadModal(newThreadModal.asWidget());
		view.setAlert(synAlert.asWidget());
	}

	private void configureThreadList() {
		// TODO Auto-generated method stub
		
	}

	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}

	public void configure(final String entityId,final String entityName) {
		tab.setEntityNameAndPlace(entityName, new Synapse(entityId, PROJECT_VERSION_NUMBER, EntityArea.DISCUSSION, areaToken));
		tab.setTabListItemVisible(DisplayUtils.isInTestWebsite(cookies));
		discussionForumClient.getForumMetadata(entityId, new AsyncCallback<Forum>(){

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(Forum result) {
				forum = result;
				newThreadModal.configure(forum.getId(), newThreadCallback);
			}
		});
	}

	public Tab asTab(){
		return tab;
	}

	@Override
	public void onClickNewThread() {
		newThreadModal.show();
	}
}
