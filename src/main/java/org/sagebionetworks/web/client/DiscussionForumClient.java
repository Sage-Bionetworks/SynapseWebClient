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
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.rpc.XsrfProtectedService;

@RemoteServiceRelativePath("discussionforumclient")	
public interface DiscussionForumClient {

	DiscussionThreadBundle createThread(CreateDiscussionThread toCreate)
			throws RestServiceException;

	PaginatedResults<DiscussionThreadBundle> getThreadsForForum(String forumId,
			Long limit, Long offset, DiscussionThreadOrder order, Boolean ascending,
			DiscussionFilter filter) throws RestServiceException;

	DiscussionThreadBundle updateThread(String threadId, UpdateThread newThread)
			throws RestServiceException;

	DiscussionThreadBundle updateThreadTitle(String threadId, UpdateThreadTitle newTitle)
			throws RestServiceException;

	DiscussionThreadBundle updateThreadMessage(String threadId, UpdateThreadMessage newMessage)
			throws RestServiceException;

	void markThreadAsDeleted(String threadId) throws RestServiceException;

	DiscussionReplyBundle createReply(CreateDiscussionReply toCreate)
			throws RestServiceException;

	PaginatedResults<DiscussionReplyBundle> getRepliesForThread(String threadId,
			Long limit, Long offset, DiscussionReplyOrder order, Boolean ascending,
			DiscussionFilter filter) throws RestServiceException;

	DiscussionReplyBundle updateReplyMessage(String replyId, UpdateReplyMessage newMessage)
			throws RestServiceException;

	void markReplyAsDeleted(String replyId) throws RestServiceException;

	Project getForumProject(String forumId) throws RestServiceException;

	void pinThread(String threadId) throws RestServiceException;

	void unpinThread(String threadId) throws RestServiceException;

	PaginatedResults<DiscussionThreadBundle> getThreadsForEntity(String entityId,
			Long limit, Long offset, DiscussionThreadOrder order, Boolean ascending,
			DiscussionFilter filter) throws RestServiceException;

	void restoreThread(String threadId) throws RestServiceException;
}
