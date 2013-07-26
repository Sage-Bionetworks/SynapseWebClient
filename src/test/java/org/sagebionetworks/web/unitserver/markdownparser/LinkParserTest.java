package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.LinkParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;

public class LinkParserTest {
	LinkParser parser;
	
	@Before
	public void setup(){
		parser = new LinkParser();
	}
	
	@Test
	public void testLink(){
		String text = "This Is A Test";
		String href = "http://example.com";
		String line = "[" + text + "](" + href +")";
		MarkdownElements elements = new MarkdownElements(line);
		parser.processLine(elements);
		String result = elements.getHtml();
		assertTrue(result.contains("<a"));
		assertTrue(result.contains("href=\"" + href));
		assertTrue(result.contains(text));
	}
	
	@Test
	public void testBookmarkAndLink() {
		String line = "I want to refer to [this](#Bookmark:subject1). To see official page, go [here](http://example.com).";
		MarkdownElements elements = new MarkdownElements(line);
		parser.processLine(elements);
		String result = elements.getHtml();
		assertTrue(result.contains("${bookmark?text=this&inlineWidget=true&bookmarkID=subject1}"));
		assertTrue(result.contains("<a class=\"link\" target=\"_blank\" href=\"http://example.com\">here</a>"));
	}
}
