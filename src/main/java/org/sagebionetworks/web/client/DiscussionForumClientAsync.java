package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.discussion.CreateDiscussionReply;
import org.sagebionetworks.repo.model.discussion.CreateDiscussionThread;
import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyOrder;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.repo.model.discussion.UpdateReplyMessage;
import org.sagebionetworks.repo.model.discussion.UpdateThreadMessage;
import org.sagebionetworks.repo.model.discussion.UpdateThreadTitle;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.discussion.UpdateThread;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("discussionforumclient")
public interface DiscussionForumClientAsync{

	void createThread(CreateDiscussionThread toCreate,
			AsyncCallback<DiscussionThreadBundle> callback);

	void getThreadsForForum(String forumId, Long limit, Long offset,
			DiscussionThreadOrder order, Boolean ascending, DiscussionFilter filter,
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

	void getRepliesForThread(String threadId, Long limit, Long offset,
			DiscussionReplyOrder order, Boolean ascending, DiscussionFilter filter,
			AsyncCallback<PaginatedResults<DiscussionReplyBundle>> callback);

	void updateReplyMessage(String replyId, UpdateReplyMessage newMessage,
			AsyncCallback<DiscussionReplyBundle> callback);

	void markReplyAsDeleted(String replyId, AsyncCallback<Void> callback);

	void getForumProject(String forumId, AsyncCallback<Project> callback);
	
	void pinThread(String threadId, AsyncCallback<Void> callback);

	void unpinThread(String threadId, AsyncCallback<Void> callback);

	void getThreadsForEntity(String entityId, Long limit, Long offset,
			DiscussionThreadOrder order, Boolean ascending, DiscussionFilter filter,
			AsyncCallback<PaginatedResults<DiscussionThreadBundle>> callback);

	void restoreThread(String threadId, AsyncCallback<Void> callback);
}
