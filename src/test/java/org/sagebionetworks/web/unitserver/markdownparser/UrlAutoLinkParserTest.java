package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import org.jsoup.Jsoup;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;
import org.sagebionetworks.web.server.markdownparser.UrlAutoLinkParser;

import com.extjs.gxt.ui.client.widget.Document;

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
		String result = elements.getHtml();
		assertTrue(result.contains("<div class=\"inline-block\" id=\"autolink_0\"></div>"));
		
		/*
		Document doc = Jsoup.parse(result);
		parser.completeParse(doc);
		assertTrue(doc.html().contains(""));
		*/
	}
}
