package org.sagebionetworks.web.unitclient.widget.discussion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;

public class DiscussionTestUtils {

	public static DiscussionThreadBundle createThreadBundle(String threadId, String title, List<String> activeAuthors, Long numberOfReplies, Long numberOfViews, Date lastActivity, String messageKey, Boolean isDeleted, String createdBy, Boolean isEdited, Boolean isPinned) {
		return createThreadBundle(threadId, null, null, title, messageKey, isEdited, isPinned, isDeleted, createdBy, null, null, lastActivity, null, activeAuthors, numberOfReplies, numberOfViews);
	}

	public static DiscussionThreadBundle createThreadBundle(String threadId, String projectId, String forumId, String title, String messageKey, Boolean isEdited, Boolean isPinned, Boolean isDeleted, String createdBy, Date createdOn, Date modifiedOn, Date lastActivity, String etag, List<String> activeAuthors, Long numberOfReplies, Long numberOfViews) {
		DiscussionThreadBundle threadBundle = new DiscussionThreadBundle();
		threadBundle.setId(threadId);
		threadBundle.setProjectId(projectId);
		threadBundle.setForumId(forumId);
		threadBundle.setTitle(title);
		threadBundle.setMessageKey(messageKey);
		threadBundle.setIsEdited(isEdited);
		threadBundle.setIsPinned(isPinned);
		threadBundle.setIsDeleted(isDeleted);
		threadBundle.setCreatedBy(createdBy);
		threadBundle.setCreatedOn(createdOn);
		threadBundle.setModifiedOn(modifiedOn);
		threadBundle.setLastActivity(lastActivity);
		threadBundle.setEtag(etag);
		threadBundle.setActiveAuthors(activeAuthors);
		threadBundle.setNumberOfReplies(numberOfReplies);
		threadBundle.setNumberOfViews(numberOfViews);
		return threadBundle;
	}

	public static List<DiscussionReplyBundle> createReplyBundleList(int numberOfBundle) {
		List<DiscussionReplyBundle> list = new ArrayList<DiscussionReplyBundle>();
		for (int i = 0; i < numberOfBundle; i++) {
			DiscussionReplyBundle bundle = new DiscussionReplyBundle();
			list.add(bundle);
		}
		return list;
	}

}
