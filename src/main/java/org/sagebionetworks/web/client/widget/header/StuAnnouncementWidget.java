package org.sagebionetworks.web.client.widget.header;

import java.util.Date;

import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.web.client.DateUtils;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.TopicUtils;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.inject.Inject;


public class StuAnnouncementWidget implements StuAnnouncementWidgetView.Presenter{
	StuAnnouncementWidgetView view;
	SynapseJSNIUtils synapseJSNIUtils;
	DiscussionForumClientAsync discussionForumClient;
	GlobalApplicationState globalApplicationState;
	ClientCache clientCache;
	CookieProvider cookies;
	public static final String STU_ANNOUNCEMENTS_FORUM_ID_KEY = "org.sagebionetworks.portal.stu_announcements_forum_id";
	public static final String STU_ANNOUNCEMENTS_PROJECT_ID_KEY = "org.sagebionetworks.portal.stu_announcements_project_id";
	public static final String STU_ANNOUNCEMENT_CLICKED_PREFIX_KEY = "org.sagebionetworks.portal.stu_announcement_clicked_";
	public static final String STU_USER_ID_KEY = "org.sagebionetworks.portal.stu_user_id";
	
	String announcementThreadId;
	@Inject
	public StuAnnouncementWidget(
			StuAnnouncementWidgetView view,
			SynapseJSNIUtils synapseJSNIUtils,
			DiscussionForumClientAsync discussionForumClient,
			GlobalApplicationState globalApplicationState,
			ClientCache clientCache, 
			CookieProvider cookies
			) {
		this.view = view;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.discussionForumClient = discussionForumClient;
		this.globalApplicationState = globalApplicationState;
		this.clientCache = clientCache;
		this.cookies = cookies;
		view.setPresenter(this);
		initStuAnnouncement();
	}

	public void initStuAnnouncement() {
		view.hide();
		if (DisplayUtils.isInTestWebsite(cookies)) {
			String forumId = globalApplicationState.getSynapseProperty(STU_ANNOUNCEMENTS_FORUM_ID_KEY);
			Long limit = 1L;
			Long offset = 0L;
			boolean ascending = false;
			discussionForumClient.getThreadsForForum(forumId, 
					limit, 
					offset, 
					DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY, 
					ascending, 
					DiscussionFilter.EXCLUDE_DELETED,
					new AsyncCallback<PaginatedResults<DiscussionThreadBundle>>(){
	
				@Override
				public void onFailure(Throwable caught) {
					synapseJSNIUtils.consoleError(caught.getMessage());
				}
	
				@Override
				public void onSuccess(PaginatedResults<DiscussionThreadBundle> threads) {
					if (threads.getResults().size() > 0) {
						DiscussionThreadBundle bundle = threads.getResults().get(0);
						announcementThreadId = bundle.getId();
						if (isShowAnnouncement(bundle)) {
							view.show(bundle.getTitle());	
						}
					}
				}
			});
		}
	}
	
	/**
	 * Announcement is only shown if it's "recent".  Let's say it's recent if it's been updated within the last 2 days, and has not already been clicked on.
	 * @param bundle
	 * @return
	 */
	public boolean isShowAnnouncement(DiscussionThreadBundle bundle) {
		// only display posts by stu
		String stuUserId = globalApplicationState.getSynapseProperty(STU_USER_ID_KEY);
		boolean show = false;
		int daysOld = CalendarUtil.getDaysBetween(bundle.getModifiedOn(), new Date());
		if (bundle.getCreatedBy().equals(stuUserId) && daysOld < 3 && !clientCache.contains(STU_ANNOUNCEMENT_CLICKED_PREFIX_KEY + announcementThreadId)) {
			show = true;
		}
		return show;
	}
	
	@Override
	public void onClickAnnouncement() {
		onDismiss();
		//go to the thread!
		String stuAnnouncementsProjectId = globalApplicationState.getSynapseProperty(STU_ANNOUNCEMENTS_PROJECT_ID_KEY);
		globalApplicationState.getPlaceChanger().goTo(TopicUtils.getThreadPlace(stuAnnouncementsProjectId, announcementThreadId));
	}
	
	@Override
	public void onDismiss() {
		clientCache.put(STU_ANNOUNCEMENT_CLICKED_PREFIX_KEY + announcementThreadId, Boolean.TRUE.toString(), DateUtils.getYearFromNow().getTime());
		view.hide();
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
