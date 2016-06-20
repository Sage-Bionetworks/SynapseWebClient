package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.TopicUtils;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.BadgeSize;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DiscussionThreadListItemWidget implements DiscussionThreadListItemWidgetView.Presenter{

	DiscussionThreadListItemWidgetView view;
	SynapseAlert synAlert;
	PortalGinInjector ginInjector;
	SynapseJSNIUtils jsniUtils;
	UserBadge authorWidget;
	GlobalApplicationState globalApplicationState;
	private CallbackP<String> threadIdClickedCallback; 
	
	private String threadId;
	private String title;
	private String projectId;
	@Inject
	public DiscussionThreadListItemWidget(
			DiscussionThreadListItemWidgetView view,
			SynapseAlert synAlert,
			UserBadge authorWidget,
			PortalGinInjector ginInjector,
			SynapseJSNIUtils jsniUtils,
			GlobalApplicationState globalApplicationState
			) {
		this.ginInjector = ginInjector;
		this.view = view;
		this.jsniUtils = jsniUtils;
		this.synAlert = synAlert;
		this.authorWidget = authorWidget;
		this.globalApplicationState = globalApplicationState;
		
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
		view.setThreadAuthor(authorWidget.asWidget());
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void configure(DiscussionThreadBundle bundle) {
		this.title = bundle.getTitle();
		this.threadId = bundle.getId();
		view.clear();
		view.setTitle(title);
		authorWidget.configure(bundle.getCreatedBy());
		authorWidget.setSize(BadgeSize.SMALL_PICTURE_ONLY);
		for (String userId : bundle.getActiveAuthors()){
			UserBadge user = ginInjector.getUserBadgeWidget();
			user.configure(userId);
			user.setSize(BadgeSize.SMALL_PICTURE_ONLY);
			view.addActiveAuthor(user.asWidget());
		}
		Long numberOfReplies = bundle.getNumberOfReplies();
		view.setNumberOfReplies(numberOfReplies.toString());
		view.setNumberOfViews(bundle.getNumberOfViews().toString());
		view.setLastActivity(jsniUtils.getRelativeTime(bundle.getLastActivity()));

		Boolean isPinned = bundle.getIsPinned();
		if (isPinned == null) {
			isPinned = false;
		}
		view.setPinnedIconVisible(isPinned);
	}
	
	@Override
	public void onClickThread() {
		if (threadIdClickedCallback == null) {
			globalApplicationState.getPlaceChanger().goTo(TopicUtils.getThreadPlace(projectId, threadId));	
		} else {
			threadIdClickedCallback.invoke(threadId);
		}
	}
	
	public void setThreadIdClickedCallback(CallbackP<String> threadIdClickedCallback) {
		this.threadIdClickedCallback = threadIdClickedCallback;
	}
}
