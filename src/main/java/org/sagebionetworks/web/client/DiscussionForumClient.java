package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.discussion.CreateDiscussionReply;
import org.sagebionetworks.repo.model.discussion.CreateDiscussionThread;
import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyOrder;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.repo.model.discussion.UpdateReplyMessage;
import org.sagebionetworks.repo.model.discussion.UpdateThreadMessage;
import org.sagebionetworks.repo.model.discussion.UpdateThreadTitle;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.discussion.UpdateThread;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("discussionforumclient")	
public interface DiscussionForumClient extends RemoteService {

	Forum getForumByProjectId(String projectId) throws RestServiceException;

	DiscussionThreadBundle createThread(CreateDiscussionThread toCreate)
			throws RestServiceException;

	DiscussionThreadBundle getThread(String threadId)
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

	DiscussionReplyBundle getReply(String replyId)
			throws RestServiceException;

	PaginatedResults<DiscussionReplyBundle> getRepliesForThread(String threadId,
			Long limit, Long offset, DiscussionReplyOrder order, Boolean ascending,
			DiscussionFilter filter) throws RestServiceException;

	DiscussionReplyBundle updateReplyMessage(String replyId, UpdateReplyMessage newMessage)
			throws RestServiceException;

	void markReplyAsDeleted(String replyId) throws RestServiceException;

	String getThreadUrl(String messageKey) throws RestServiceException;

	String getReplyUrl(String messageKey) throws RestServiceException;

	Project getForumProject(String forumId) throws RestServiceException;

	Long getThreadCountForForum(String forumId, DiscussionFilter filter) throws RestServiceException;

	Long getReplyCountForThread(String threadId, DiscussionFilter filter) throws RestServiceException;
	
	void pinThread(String threadId) throws RestServiceException;
	void unpinThread(String threadId) throws RestServiceException;
}
