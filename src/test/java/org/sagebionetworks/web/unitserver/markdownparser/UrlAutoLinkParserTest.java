package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;
import org.sagebionetworks.web.server.markdownparser.UrlAutoLinkParser;

public class UrlAutoLinkParserTest {
	UrlAutoLinkParser parser;
	
	@Before
	public void setup() {
		parser = new UrlAutoLinkParser();
	}
	
	@Test
	public void testAutoLink() {
		String line = "Go to this link http://www.example.com";
		MarkdownElements elements = new MarkdownElements(line);
		String encodedUrl = WidgetEncodingUtil.encodeValue("http://www.example.com");
		parser.processLine(elements);
		String result = elements.getMarkdown();
		assertTrue(result.contains("${link?text=" + encodedUrl + "&url=" + encodedUrl + "&inlineWidget=true}"));
		
	}
}
