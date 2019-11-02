package org.sagebionetworks.web.client.utils;

import static org.sagebionetworks.web.client.place.Synapse.SYNAPSE_ENTITY_PREFIX;
import static org.sagebionetworks.web.client.widget.discussion.ForumWidget.REPLY_ID_KEY;
import static org.sagebionetworks.web.client.widget.discussion.ForumWidget.THREAD_ID_KEY;
import org.sagebionetworks.web.client.place.Synapse;

public class TopicUtils {

	public static Synapse getForumPlace(String projectId) {
		return new Synapse(projectId, null, Synapse.EntityArea.DISCUSSION, null);
	}

	public static String buildForumLink(String projectId) {
		Synapse place = getForumPlace(projectId);
		String link = "/" + SYNAPSE_ENTITY_PREFIX + place.toToken();
		return link;
	}

	public static Synapse getThreadPlace(String projectId, String threadId) {
		String token = THREAD_ID_KEY + "=" + threadId;
		return new Synapse(projectId, null, Synapse.EntityArea.DISCUSSION, token);
	}

	public static String buildThreadLink(String projectId, String threadId) {
		Synapse place = getThreadPlace(projectId, threadId);
		String link = "/" + SYNAPSE_ENTITY_PREFIX + place.toToken();
		return link;
	}

	public static Synapse getReplyPlace(String projectId, String threadId, String replyId) {
		String token = THREAD_ID_KEY + "=" + threadId + "&" + REPLY_ID_KEY + "=" + replyId;
		return new Synapse(projectId, null, Synapse.EntityArea.DISCUSSION, token);
	}

	public static String buildReplyLink(String projectId, String threadId, String replyId) {
		Synapse place = getReplyPlace(projectId, threadId, replyId);
		String link = "/" + SYNAPSE_ENTITY_PREFIX + place.toToken();
		return link;
	}
}
