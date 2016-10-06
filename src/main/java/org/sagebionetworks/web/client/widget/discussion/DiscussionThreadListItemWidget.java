package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.user.BadgeSize;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DiscussionThreadListItemWidget implements DiscussionThreadListItemWidgetView.Presenter{

	DiscussionThreadListItemWidgetView view;
	PortalGinInjector ginInjector;
	SynapseJSNIUtils jsniUtils;
	UserBadge authorWidget;
	private CallbackP<DiscussionThreadBundle> threadIdClickedCallback; 
	
	private DiscussionThreadBundle bundle;
	@Inject
	public DiscussionThreadListItemWidget(
			DiscussionThreadListItemWidgetView view,
			UserBadge authorWidget,
			PortalGinInjector ginInjector,
			SynapseJSNIUtils jsniUtils
			) {
		this.ginInjector = ginInjector;
		this.view = view;
		this.jsniUtils = jsniUtils;
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
		if (threadIdClickedCallback != null) {
			threadIdClickedCallback.invoke(bundle);
		}
	}
	
	public void setThreadIdClickedCallback(CallbackP<DiscussionThreadBundle> threadIdClickedCallback) {
		this.threadIdClickedCallback = threadIdClickedCallback;
	}
}
