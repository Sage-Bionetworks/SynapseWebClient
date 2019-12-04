package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.TopicUtils;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DiscussionThreadListItemWidget implements DiscussionThreadListItemWidgetView.Presenter {

	DiscussionThreadListItemWidgetView view;
	PortalGinInjector ginInjector;
	DateTimeUtils dateTimeUtils;
	UserBadge authorWidget;
	private CallbackP<DiscussionThreadBundle> threadIdClickedCallback;

	private DiscussionThreadBundle bundle;

	@Inject
	public DiscussionThreadListItemWidget(DiscussionThreadListItemWidgetView view, UserBadge authorWidget, PortalGinInjector ginInjector, DateTimeUtils dateTimeUtils) {
		this.ginInjector = ginInjector;
		this.view = view;
		this.dateTimeUtils = dateTimeUtils;
		this.authorWidget = authorWidget;

		view.setPresenter(this);
		view.setThreadAuthor(authorWidget.asWidget());
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void configure(DiscussionThreadBundle bundle) {
		this.bundle = bundle;
		view.setTitle(bundle.getTitle());
		view.setThreadUrl(TopicUtils.buildThreadLink(bundle.getProjectId(), bundle.getId()));
		authorWidget.setTextHidden(true);
		authorWidget.configure(bundle.getCreatedBy());
		view.clearActiveAuthors();
		for (String userId : bundle.getActiveAuthors()) {
			UserBadge user = ginInjector.getUserBadgeWidget();
			user.setTextHidden(true);
			user.configure(userId);
			view.addActiveAuthor(user.asWidget());
		}
		Long numberOfReplies = bundle.getNumberOfReplies();
		view.setNumberOfReplies(numberOfReplies.toString());
		view.setNumberOfViews(bundle.getNumberOfViews().toString());
		view.setLastActivity(dateTimeUtils.getRelativeTime(bundle.getLastActivity()));

		Boolean isPinned = bundle.getIsPinned();
		if (isPinned == null) {
			isPinned = false;
		}
		view.setPinnedIconVisible(isPinned);
	}

	@Override
	public void onClickThread() {
		if (threadIdClickedCallback != null) {
			threadIdClickedCallback.invoke(bundle);
		}
	}

	public void setThreadIdClickedCallback(CallbackP<DiscussionThreadBundle> threadIdClickedCallback) {
		this.threadIdClickedCallback = threadIdClickedCallback;
	}
}
