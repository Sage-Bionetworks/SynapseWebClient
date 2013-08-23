package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import org.sagebionetworks.web.server.ServerMarkdownUtils;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;
import org.sagebionetworks.web.server.markdownparser.UrlAutoLinkParser;

public class UrlAutoLinkParserTest {
	UrlAutoLinkParser parser;
	
	@Before
	public void setup() {
		parser = new UrlAutoLinkParser();
		parser.reset();
	}
	
	@Test
	public void testAutoLink() {
		String line = "Go to this link http://www.example.com";
		MarkdownElements elements = new MarkdownElements(line);
		parser.processLine(elements, null);
		String result = elements.getHtml();
		assertTrue(!result.contains("http://www.example.com"));
		assertTrue(result.contains(ServerMarkdownUtils.START_CONTAINER));
		assertTrue(result.contains(ServerMarkdownUtils.END_CONTAINER));

		Document doc = Jsoup.parse(result);
		parser.completeParse(doc);
		System.out.println("Doc: " + doc.html());
		assertTrue(doc.html().contains("http://www.example.com"));
		assertTrue(doc.html().contains("<a"));
		assertTrue(doc.html().contains("</a>"));
	}
}
