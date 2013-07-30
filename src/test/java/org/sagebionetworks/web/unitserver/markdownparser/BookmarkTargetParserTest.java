package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.BookmarkTargetParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;

public class BookmarkTargetParserTest {
	BookmarkTargetParser parser;
	
	@Before
	public void setup() {
		parser = new BookmarkTargetParser();
	}
	
	@Test
	public void testBookmarkTarget() {
		String text = "${bookmarktarget?bookmarkID=head2} Heading 2";
		MarkdownElements elements = new MarkdownElements(text);
		parser.processLine(elements);
		assertTrue(elements.getHtml().contains("<p id=\"head2\"></p>"));
	}
}
