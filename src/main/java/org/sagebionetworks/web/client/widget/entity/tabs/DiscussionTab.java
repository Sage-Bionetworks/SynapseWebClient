package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.modal.NewDiscussionThreadModal;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class DiscussionTab implements DiscussionTabView.Presenter{
	private final static Long PROJECT_VERSION_NUMBER = null;
	public final static Boolean DEFAULT_MODERATOR_MODE = false;

	Tab tab;
	DiscussionTabView view;
	CookieProvider cookies;
	// TODO: use this token to navigate between threads within the discussion tab
	String areaToken = null;
	NewDiscussionThreadModal newThreadModal;
	DiscussionThreadListWidget threadListWidget;
	SynapseAlert synAlert;
	DiscussionForumClientAsync discussionForumClient;
	private String forumId;

	@Inject
	public DiscussionTab(
			DiscussionTabView view,
			Tab tab,
			SynapseAlert synAlert,
			DiscussionForumClientAsync discussionForumClient,
			DiscussionThreadListWidget threadListWidget,
			NewDiscussionThreadModal newThreadModal,
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
		tab.setTabListItemVisible(DisplayUtils.isInTestWebsite(cookies));
	}

	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}

	public void configure(final String entityId, final String entityName, final Boolean isCurrentUserModerator) {
		tab.setEntityNameAndPlace(entityName, new Synapse(entityId, PROJECT_VERSION_NUMBER, EntityArea.DISCUSSION, areaToken));
		tab.setTabListItemVisible(DisplayUtils.isInTestWebsite(cookies));
		discussionForumClient.getForumMetadata(entityId, new AsyncCallback<Forum>(){

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(final Forum forum) {
				forumId = forum.getId();
				newThreadModal.configure(forumId, new Callback(){
					@Override
					public void invoke() {
						threadListWidget.configure(forumId, DEFAULT_MODERATOR_MODE);
					}
				});
				threadListWidget.configure(forumId, DEFAULT_MODERATOR_MODE);
			}
		});
		view.setModeratorModeContainerVisibility(isCurrentUserModerator);
	}

	public Tab asTab(){
		return tab;
	}

	@Override
	public void onClickNewThread() {
		newThreadModal.show();
	}

	@Override
	public void onModeratorModeChange() {
		threadListWidget.configure(forumId, view.getModeratorMode());
	}
}
