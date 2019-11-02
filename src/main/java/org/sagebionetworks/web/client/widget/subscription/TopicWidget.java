package org.sagebionetworks.web.client.widget.subscription;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.TopicUtils;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TopicWidget implements TopicWidgetView.Presenter, SynapseWidgetPresenter {

	public static final String DATA_ACCESS_SUBMISSION_TOPIC_TEXT = "Notifications";
	private TopicWidgetView view;
	DiscussionForumClientAsync forumClient;
	SynapseAlert synAlert;
	SynapseJavascriptClient jsClient;

	@Inject
	public TopicWidget(TopicWidgetView view, DiscussionForumClientAsync forumClient, SynapseAlert synAlert, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.forumClient = forumClient;
		fixServiceEntryPoint(forumClient);
		this.synAlert = synAlert;
		this.jsClient = jsClient;
		view.setPresenter(this);
	}

	/**
	 * @param type Topic subscription object type
	 * @param id Topic subscription object id
	 */
	public void configure(SubscriptionObjectType type, String id) {
		synAlert.clear();
		switch (type) {
			case THREAD:
				configureThread(id);
				break;
			case FORUM:
				configureForum(id);
				break;
			case DATA_ACCESS_SUBMISSION:
				view.setTopicText(DATA_ACCESS_SUBMISSION_TOPIC_TEXT);
				view.setTopicHref("");
				view.setIcon(IconType.MAIL_FORWARD);
				break;
			default:
				synAlert.showError("Unknown topic type: " + type);
				break;
		}
	}

	public void configureThread(String threadId) {
		// get the thread Project and thread title
		jsClient.getThread(threadId, new AsyncCallback<DiscussionThreadBundle>() {
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
		// get the forum Project
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
