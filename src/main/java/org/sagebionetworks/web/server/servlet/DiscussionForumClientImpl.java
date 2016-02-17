package org.sagebionetworks.web.server.servlet;

import java.net.URL;

import org.sagebionetworks.client.exceptions.SynapseException;
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
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

@SuppressWarnings("serial")
public class DiscussionForumClientImpl extends SynapseClientBase implements
		DiscussionForumClient{

	/**
	 * Helper to convert from the non-gwt compatible PaginatedResults to the compatible type.
	 * @param in
	 * @return
	 */
	public <T extends JSONEntity> PaginatedResults<T> convertPaginated(org.sagebionetworks.reflection.model.PaginatedResults<T> in){
		return  new PaginatedResults<T>(in.getResults(), in.getTotalNumberOfResults());
	}

	@Override
	public Forum getForumMetadata(String projectId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getForumMetadata(projectId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public DiscussionThreadBundle createThread(CreateDiscussionThread toCreate)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createThread(toCreate);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public DiscussionThreadBundle getThread(String threadId)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getThread(threadId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public PaginatedResults<DiscussionThreadBundle> getThreadsForForum(
			String forumId, Long limit, Long offset,
			DiscussionThreadOrder order, Boolean ascending, DiscussionFilter filter)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return convertPaginated(synapseClient.getThreadsForForum(forumId, limit, offset, order, ascending, filter));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public DiscussionThreadBundle updateThreadTitle(String threadId,
			UpdateThreadTitle newTitle) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.updateThreadTitle(threadId, newTitle);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public DiscussionThreadBundle updateThreadMessage(String threadId,
			UpdateThreadMessage newMessage) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.updateThreadMessage(threadId, newMessage);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void markThreadAsDeleted(String threadId)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.markThreadAsDeleted(threadId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public DiscussionReplyBundle createReply(CreateDiscussionReply toCreate)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createReply(toCreate);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public DiscussionReplyBundle getReply(String replyId)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getReply(replyId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public PaginatedResults<DiscussionReplyBundle> getRepliesForThread(
			String threadId, Long limit, Long offset,
			DiscussionReplyOrder order, Boolean ascending, DiscussionFilter filter)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return convertPaginated(synapseClient.getRepliesForThread(threadId, limit, offset, order, ascending, filter));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public DiscussionReplyBundle updateReplyMessage(String replyId,
			UpdateReplyMessage newMessage) throws RestServiceException {
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
	public DiscussionThreadBundle updateThread(String threadId,
			UpdateThread newThread) throws RestServiceException {
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
	public String getThreadMessage(String messageKey) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			URL presignedURL = synapseClient.getThreadUrl(messageKey);
			return doGet(presignedURL);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (RequestException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public String getReplyMessage(String messageKey) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			URL presignedURL = synapseClient.getReplyUrl(messageKey);
			return doGet(presignedURL);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (RequestException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	public String doGet(URL url) throws RequestException{
		final StringBuilder message = new StringBuilder();
		RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url.toString());
		requestBuilder.sendRequest(null, new RequestCallback(){

			@Override
			public void onResponseReceived(Request request,
					Response response) {
				if (response.getStatusCode() == Response.SC_OK) {
					message.append(response.getText());
				} else {
					onError(null, new IllegalArgumentException(response.getStatusText()));
				}
			}

			@Override
			public void onError(Request request, Throwable exception) {
				throw new IllegalArgumentException(exception.getMessage());
			}
		});
		return message.toString();
	}
}
