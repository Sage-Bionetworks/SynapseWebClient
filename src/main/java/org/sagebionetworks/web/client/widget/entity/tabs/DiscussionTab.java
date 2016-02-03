package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadWidget;
import org.sagebionetworks.web.client.widget.discussion.modal.NewDiscussionThreadModal;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class DiscussionTab implements DiscussionTabView.Presenter{
	private final static Long PROJECT_VERSION_NUMBER = null;
	//used to tell the discussion tab to show a single thread
	public final static String THREAD_ID_KEY = "threadId";
	Tab tab;
	DiscussionTabView view;
	CookieProvider cookies;
	//use this token to navigate between threads within the discussion tab
	ParameterizedToken params;
	 
	NewDiscussionThreadModal newThreadModal;
	DiscussionThreadListWidget threadListWidget;
	SynapseAlert synAlert;
	DiscussionForumClientAsync discussionForumClient;
	DiscussionThreadWidget singleThreadWidget;
	String entityName, entityId;
	
	@Inject
	public DiscussionTab(
			DiscussionTabView view,
			Tab tab,
			SynapseAlert synAlert,
			DiscussionForumClientAsync discussionForumClient,
			DiscussionThreadListWidget threadListWidget,
			NewDiscussionThreadModal newThreadModal,
			CookieProvider cookies,
			DiscussionThreadWidget singleThreadWidget
			) {
		this.view = view;
		this.tab = tab;
		this.synAlert = synAlert;
		this.threadListWidget = threadListWidget;
		this.newThreadModal = newThreadModal;
		this.discussionForumClient = discussionForumClient;
		this.cookies = cookies;
		this.singleThreadWidget = singleThreadWidget;
		tab.configure("Discussion", view.asWidget());
		view.setPresenter(this);
		view.setThreadList(threadListWidget.asWidget());
		view.setNewThreadModal(newThreadModal.asWidget());
		view.setAlert(synAlert.asWidget());
		view.setSingleThread(singleThreadWidget.asWidget());
		tab.setTabListItemVisible(DisplayUtils.isInTestWebsite(cookies));
	}

	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}

	public void configure(String entityId, String entityName, String areaToken) {
		this.entityId = entityId;
		this.entityName = entityName;
		params = new ParameterizedToken(areaToken);
		updatePlace();
		tab.setTabListItemVisible(DisplayUtils.isInTestWebsite(cookies));
		
		//are we just showing a single thread, or the full list?
		if (params.containsKey(THREAD_ID_KEY)) {
			String threadId = params.get(THREAD_ID_KEY);
			showThread(threadId);
		} else {
			showForum();
		}
	}

	/**
	 * Based on the current area parameters, update the address bar (push the url in to the browser history).
	 */
	public void updatePlace(){
		tab.setEntityNameAndPlace(entityName, new Synapse(entityId, PROJECT_VERSION_NUMBER, EntityArea.DISCUSSION, params.toString()));
	}
	public void showThread(String threadId) {
		view.setSingleThreadUIVisible(true);
		view.setThreadListUIVisible(false);
		
		discussionForumClient.getThread(threadId, new AsyncCallback<DiscussionThreadBundle>(){
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionThreadBundle result) {
				singleThreadWidget.configure(result);
				if (singleThreadWidget.isThreadCollapsed()) {
					singleThreadWidget.toggleThread();	
				}
			}
		});
	}
	
	public void showForum() {
		view.setSingleThreadUIVisible(false);
		view.setThreadListUIVisible(true);
		discussionForumClient.getForumMetadata(entityId, new AsyncCallback<Forum>(){
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(final Forum forum) {
				newThreadModal.configure(forum.getId(), new Callback(){
					@Override
					public void invoke() {
						threadListWidget.configure(forum.getId());
					}
				});
				threadListWidget.configure(forum.getId());
			}
		});
	}
	
	@Override
	public void onClickShowAllThreads() {
		//clear parameters
		params.clear();
		updatePlace();
		tab.showTab();
		showForum();
	}
	
	public Tab asTab(){
		return tab;
	}

	@Override
	public void onClickNewThread() {
		newThreadModal.show();
	}
}
