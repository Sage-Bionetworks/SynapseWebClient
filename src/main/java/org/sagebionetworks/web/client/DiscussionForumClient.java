package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.discussion.CreateDiscussionReply;
import org.sagebionetworks.repo.model.discussion.CreateDiscussionThread;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.UpdateReplyMessage;
import org.sagebionetworks.web.shared.discussion.UpdateThread;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("discussionforumclient")
public interface DiscussionForumClient extends RemoteService {

	DiscussionThreadBundle createThread(CreateDiscussionThread toCreate) throws RestServiceException;

	DiscussionThreadBundle updateThread(String threadId, UpdateThread newThread) throws RestServiceException;

	void markThreadAsDeleted(String threadId) throws RestServiceException;

	DiscussionReplyBundle createReply(CreateDiscussionReply toCreate) throws RestServiceException;

	DiscussionReplyBundle updateReplyMessage(String replyId, UpdateReplyMessage newMessage) throws RestServiceException;

	void markReplyAsDeleted(String replyId) throws RestServiceException;

	Project getForumProject(String forumId) throws RestServiceException;

	void pinThread(String threadId) throws RestServiceException;

	void unpinThread(String threadId) throws RestServiceException;

	void restoreThread(String threadId) throws RestServiceException;
}
