package org.sagebionetworks.web.client.widget.subscription;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.utils.TopicUtils;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TopicWidget implements TopicWidgetView.Presenter, SynapseWidgetPresenter {
	
	private TopicWidgetView view;
	DiscussionForumClientAsync forumClient;
	SynapseAlert synAlert;
	
	@Inject
	public TopicWidget(TopicWidgetView view, 
			DiscussionForumClientAsync forumClient,
			SynapseAlert synAlert) {
		this.view = view;
		this.forumClient = forumClient;
		this.synAlert = synAlert;
		view.setPresenter(this);
	}
	
	/**
	 * @param type Topic subscription object type
	 * @param id Topic subscription object id
	 */
	public void configure(SubscriptionObjectType type, String id) {
		synAlert.clear();
		switch (type) {
			case DISCUSSION_THREAD:
			case THREAD:
				configureThread(id);
				break;
			case FORUM:
				configureForum(id);
				break;			
			default:
				synAlert.showError("Unknown topic type: " + type);
				break;
		}
	}
	
	public void configureThread(String threadId) {
		//get the thread Project and thread title
		forumClient.getThread(threadId, new AsyncCallback<DiscussionThreadBundle>() {
			@Override
			public void onSuccess(DiscussionThreadBundle threadBundle) {
				view.setTopicText(threadBundle.getTitle());
				String href = TopicUtils.buildThreadLink(threadBundle.getProjectId(), threadBundle.getId());
				view.setTopicHref(href);
				view.setIcon(IconType.COMMENTS);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.showError(caught.getMessage());
			}
		});
	}
	
	public void configureForum(String forumId) {
		//get the forum Project
		forumClient.getForumProject(forumId, new AsyncCallback<Project>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.showError(caught.getMessage());
			}
			public void onSuccess(Project result) {
				view.setTopicText(result.getName());
				String href = TopicUtils.buildForumLink(result.getId());
				view.setTopicHref(href);
				view.setIcon(IconType.LIST_ALT);
			};
		});
	}
	
	public void addStyleNames(String styleNames) {
		view.addStyleNames(styleNames);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
