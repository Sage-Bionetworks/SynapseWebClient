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
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("discussionforumclient")	
public interface DiscussionForumClient extends RemoteService {

	Forum getForumMetadata(String projectId) throws RestServiceException;

	DiscussionThreadBundle createThread(CreateDiscussionThread toCreate)
			throws RestServiceException;

	DiscussionThreadBundle getThread(String threadId)
			throws RestServiceException;

	PaginatedResults<DiscussionThreadBundle> getThreadsForForum(String forumId,
			Long limit, Long offset, DiscussionThreadOrder order, Boolean ascending)
			throws RestServiceException;

	DiscussionThreadBundle updateThreadTitle(String threadId, UpdateThreadTitle newTitle)
			throws RestServiceException;

	DiscussionThreadBundle updateThreadMessage(String threadId, UpdateThreadMessage newMessage)
			throws RestServiceException;

	void markThreadAsDeleted(String threadId) throws RestServiceException;

	DiscussionReplyBundle createReply(CreateDiscussionReply toCreate)
			throws RestServiceException;

	DiscussionReplyBundle getReply(String replyId)
			throws RestServiceException;

	PaginatedResults<DiscussionReplyBundle> getRepliesForThread(String threadId,
			Long limit, Long offset, DiscussionReplyOrder order, Boolean ascending)
			throws RestServiceException;

	DiscussionReplyBundle updateReplyMessage(String replyId, UpdateReplyMessage newMessage)
			throws RestServiceException;

	void markReplyAsDeleted(String replyId) throws RestServiceException;
}
