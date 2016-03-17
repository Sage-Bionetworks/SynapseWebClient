package org.sagebionetworks.web.client.utils;

import static org.sagebionetworks.web.client.place.Synapse.SYNAPSE_ENTITY_PREFIX;
import static org.sagebionetworks.web.client.widget.discussion.ForumWidget.THREAD_ID_KEY;

import org.sagebionetworks.web.client.place.Synapse;

public class TopicUtils {
	public static String buildForumLink(String projectId) {
		Synapse place = new Synapse(projectId, null, Synapse.EntityArea.DISCUSSION, null);
		String link = "/" + SYNAPSE_ENTITY_PREFIX + place.toToken();
		return link;
	}

	public static String buildThreadLink(String projectId, String threadId) {
		String token = THREAD_ID_KEY+"="+threadId;
		Synapse place = new Synapse(projectId, null, Synapse.EntityArea.DISCUSSION, token);
		String link = "/" + SYNAPSE_ENTITY_PREFIX + place.toToken();
		return link;
	}
}
