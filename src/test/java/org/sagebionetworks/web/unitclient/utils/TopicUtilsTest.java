package org.sagebionetworks.web.unitclient.utils;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.sagebionetworks.web.client.utils.TopicUtils;

public class TopicUtilsTest {

	@Test
	public void testBuildReplyThreadLink() {
		String projectId = "syn123";
		String threadId = "456";
		String replyId = "987";
		assertEquals("/#!Synapse:syn123/discussion/threadId=456&replyId=987", TopicUtils.buildReplyLink(projectId, threadId, replyId));
	}

	@Test
	public void testBuildThreadLink() {
		String projectId = "syn123";
		String threadId = "456";
		assertEquals("/#!Synapse:syn123/discussion/threadId=456", TopicUtils.buildThreadLink(projectId, threadId));
	}

	@Test
	public void testBuildForumLink() {
		String projectId = "syn123";
		assertEquals("/#!Synapse:syn123/discussion/", TopicUtils.buildForumLink(projectId));
	}
}
