package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.client.exceptions.SynapseException;
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
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.DiscussionForumClient;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.discussion.UpdateThread;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

@SuppressWarnings("serial")
public class DiscussionForumClientImpl extends SynapseClientBase implements DiscussionForumClient {

	/**
	 * Helper to convert from the non-gwt compatible PaginatedResults to the compatible type.
	 * 
	 * @param in
	 * @return
	 */
	public <T extends JSONEntity> PaginatedResults<T> convertPaginated(org.sagebionetworks.reflection.model.PaginatedResults<T> in) {
		return new PaginatedResults<T>(in.getResults(), in.getTotalNumberOfResults());
	}

	public Forum getForumByProjectId(String projectId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getForumByProjectId(projectId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public DiscussionThreadBundle createThread(CreateDiscussionThread toCreate) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createThread(toCreate);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public DiscussionThreadBundle getThread(String threadId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getThread(threadId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public PaginatedResults<DiscussionThreadBundle> getThreadsForForum(String forumId, Long limit, Long offset, DiscussionThreadOrder order, Boolean ascending, DiscussionFilter filter) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return convertPaginated(synapseClient.getThreadsForForum(forumId, limit, offset, order, ascending, filter));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void markThreadAsDeleted(String threadId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.markThreadAsDeleted(threadId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public DiscussionReplyBundle createReply(CreateDiscussionReply toCreate) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createReply(toCreate);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public DiscussionReplyBundle getReply(String replyId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getReply(replyId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public PaginatedResults<DiscussionReplyBundle> getRepliesForThread(String threadId, Long limit, Long offset, DiscussionReplyOrder order, Boolean ascending, DiscussionFilter filter) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return convertPaginated(synapseClient.getRepliesForThread(threadId, limit, offset, order, ascending, filter));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public DiscussionReplyBundle updateReplyMessage(String replyId, UpdateReplyMessage newMessage) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.updateReplyMessage(replyId, newMessage);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void markReplyAsDeleted(String replyId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.markReplyAsDeleted(replyId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public DiscussionThreadBundle updateThread(String threadId, UpdateThread newThread) throws RestServiceException {
		UpdateThreadTitle updateTitle = new UpdateThreadTitle();
		updateTitle.setTitle(newThread.getTitle());
		UpdateThreadMessage updateMessage = new UpdateThreadMessage();
		updateMessage.setMessageMarkdown(newThread.getMessage());
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.updateThreadTitle(threadId, updateTitle);
			return synapseClient.updateThreadMessage(threadId, updateMessage);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public String getThreadUrl(String messageKey) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getThreadUrl(messageKey).toString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public String getReplyUrl(String messageKey) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getReplyUrl(messageKey).toString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public Project getForumProject(String forumId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			Forum forum = synapseClient.getForum(forumId);
			return (Project) synapseClient.getEntityById(forum.getProjectId());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void pinThread(String threadId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.pinThread(threadId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void unpinThread(String threadId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.unpinThread(threadId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void restoreThread(String threadId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.restoreDeletedThread(threadId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
}
