package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.discussion.CreateDiscussionThread;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.repo.model.discussion.Forum;
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

	void updateThreadTitle(String threadId, UpdateThreadTitle newTitle,
			AsyncCallback<DiscussionThreadBundle> callback);

	void updateThreadMessage(String threadId, UpdateThreadMessage newMessage,
			AsyncCallback<DiscussionThreadBundle> callback);

	void markThreadAsDeleted(String threadId, AsyncCallback<Void> callback);
}
