package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.discussion.CreateDiscussionReply;
import org.sagebionetworks.repo.model.discussion.CreateDiscussionThread;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyOrder;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.repo.model.discussion.UpdateReplyMessage;
import org.sagebionetworks.repo.model.discussion.UpdateThreadMessage;
import org.sagebionetworks.repo.model.discussion.UpdateThreadTitle;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("discussionforumclient")
public interface DiscussionForumClientAsync{

	void getForumMetadata(String projectId, AsyncCallback<Forum> callback);

	void createThread(CreateDiscussionThread toCreate,
			AsyncCallback<DiscussionThreadBundle> callback);

	void getThread(String threadId, AsyncCallback<DiscussionThreadBundle> callback);

	void getThreadsForForum(String forumId, Long limit, Long offset,
			DiscussionThreadOrder order, Boolean ascending,
			AsyncCallback<PaginatedResults<DiscussionThreadBundle>> callback);

	void updateThread(String threadId, UpdateThread newThread,
			AsyncCallback<DiscussionThreadBundle> callback);

	void updateThreadTitle(String threadId, UpdateThreadTitle newTitle,
			AsyncCallback<DiscussionThreadBundle> callback);

	void updateThreadMessage(String threadId, UpdateThreadMessage newMessage,
			AsyncCallback<DiscussionThreadBundle> callback);

	void markThreadAsDeleted(String threadId, AsyncCallback<Void> callback);

	void createReply(CreateDiscussionReply toCreate,
			AsyncCallback<DiscussionReplyBundle> callback);

	void getReply(String replyId, AsyncCallback<DiscussionReplyBundle> callback);

	void getRepliesForThread(String threadId,
			Long limit, Long offset, DiscussionReplyOrder order, Boolean ascending,
			AsyncCallback<PaginatedResults<DiscussionReplyBundle>> callback);

	void updateReplyMessage(String replyId, UpdateReplyMessage newMessage,
			AsyncCallback<DiscussionReplyBundle> callback);

	void markReplyAsDeleted(String replyId, AsyncCallback<Void> callback);
}
