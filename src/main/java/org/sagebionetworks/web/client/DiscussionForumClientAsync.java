package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.discussion.CreateDiscussionReply;
import org.sagebionetworks.repo.model.discussion.CreateDiscussionThread;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.UpdateReplyMessage;
import org.sagebionetworks.web.shared.discussion.UpdateThread;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("discussionforumclient")
public interface DiscussionForumClientAsync {

	void createThread(CreateDiscussionThread toCreate, AsyncCallback<DiscussionThreadBundle> callback);

	void updateThread(String threadId, UpdateThread newThread, AsyncCallback<DiscussionThreadBundle> callback);

	void markThreadAsDeleted(String threadId, AsyncCallback<Void> callback);

	void createReply(CreateDiscussionReply toCreate, AsyncCallback<DiscussionReplyBundle> callback);

	void updateReplyMessage(String replyId, UpdateReplyMessage newMessage, AsyncCallback<DiscussionReplyBundle> callback);

	void markReplyAsDeleted(String replyId, AsyncCallback<Void> callback);

	void getForumProject(String forumId, AsyncCallback<Project> callback);

	void pinThread(String threadId, AsyncCallback<Void> callback);

	void unpinThread(String threadId, AsyncCallback<Void> callback);

	void restoreThread(String threadId, AsyncCallback<Void> callback);
}
