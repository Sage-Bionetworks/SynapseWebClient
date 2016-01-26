package org.sagebionetworks.web.unitclient.widget.discussion;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sagebionetworks.web.client.widget.discussion.DiscussionMessageURLUtil;
import org.sagebionetworks.web.shared.WebConstants;

public class DiscussionMessageURLUtilTest {

	@Test (expected=IllegalArgumentException.class)
	public void testBuildMessageUrlWithNullKey() {
		DiscussionMessageURLUtil.buildMessageUrl(null, "type");
	}

	@Test (expected=IllegalArgumentException.class)
	public void testBuildMessageUrlWithNullType() {
		DiscussionMessageURLUtil.buildMessageUrl("key", null);
	}

	@Test
	public void testBuildMessageUrlWithThreadType() {
		String url = DiscussionMessageURLUtil.buildMessageUrl("key", WebConstants.THREAD_TYPE);
		assertNotNull(url);
		assertEquals(url, "/Portal"+WebConstants.DISCUSSION_MESSAGE_SERVLET+"?"
				+WebConstants.MESSAGE_KEY_PARAM+"=key&"+WebConstants.TYPE_PARAM
				+"="+WebConstants.THREAD_TYPE);
	}

	@Test
	public void testBuildMessageUrlWithReplyType() {
		String url = DiscussionMessageURLUtil.buildMessageUrl("key", WebConstants.REPLY_TYPE);
		assertNotNull(url);
		assertEquals(url, "/Portal"+WebConstants.DISCUSSION_MESSAGE_SERVLET+"?"
				+WebConstants.MESSAGE_KEY_PARAM+"=key&"+WebConstants.TYPE_PARAM
				+"="+WebConstants.REPLY_TYPE);
	}

	@Test (expected=IllegalArgumentException.class)
	public void testBuildMessageUrlWithUnsupportedType() {
		DiscussionMessageURLUtil.buildMessageUrl("key", "type");
	}
}
