package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;
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
		String encodedHref = WidgetEncodingUtil.encodeValue(href);
		String line = "[" + text + "](" + href +")";
		MarkdownElements elements = new MarkdownElements(line);
		parser.processLine(elements);
		String result = elements.getMarkdown();
		assertEquals(result, "${link?text=This Is A Test&url=" + encodedHref + "&inlineWidget=true}");
		
		String text2 = "Synapse";
		String href2 = "#!Synapse:syn12345";
		String encodedHref2 = WidgetEncodingUtil.encodeValue(href2);
		String line2 = "[" + text2 + "](" + href2 +")";
		MarkdownElements elements2 = new MarkdownElements(line2);
		parser.processLine(elements2);
		String result2 = elements2.getMarkdown();
		assertTrue(result2.contains("${link?text=Synapse&url=" + encodedHref2 + "&inlineWidget=true}"));
		
		String text3 = "Synapse";
		String href3 = "#Synapse:syn12345";
		String encodedHref3 = WidgetEncodingUtil.encodeValue(href3);
		String line3 = "[" + text3 + "](" + href3 +")";
		MarkdownElements elements3 = new MarkdownElements(line3);
		parser.processLine(elements3);
		String result3 = elements3.getMarkdown();
		assertTrue(result3.contains("${link?text=Synapse&url=" + encodedHref3 + "&inlineWidget=true}"));
		
	}
	
	@Test
	public void testForCompleteness() {
		String text = "Test";
		String href = "www.example.com";
		String line = "[" + text + "](" + href +")";
		MarkdownElements elements = new MarkdownElements(line);
		parser.processLine(elements);
		String result = elements.getMarkdown();
		String decodedResult = WidgetEncodingUtil.decodeValue(result);
		assertTrue(decodedResult.contains("http://www.example.com"));
		
		String text2 = "Test";
		String href2 = "example.com";
		String line2 = "[" + text2 + "](" + href2 +")";
		MarkdownElements elements2 = new MarkdownElements(line2);
		parser.processLine(elements2);
		String result2 = elements2.getMarkdown();
		String decodedResult2 = WidgetEncodingUtil.decodeValue(result2);
		assertTrue(decodedResult2.contains("http://example.com"));
		
		String text3 = "Test";
		String href3 = "ftp://ftp.example";
		String line3 = "[" + text3 + "](" + href3 +")";
		MarkdownElements elements3 = new MarkdownElements(line3);
		parser.processLine(elements3);
		String result3 = elements3.getMarkdown();
		String decodedResult3 = WidgetEncodingUtil.decodeValue(result3);
		assertTrue(decodedResult3.contains("ftp://ftp.example"));
		
	}
	
	@Test
	public void testBookmarkAndLink() {
		String line = "I want to refer to [this](#Bookmark:subject1). To see official page, go [here](http://example.com).";
		MarkdownElements elements = new MarkdownElements(line);
		parser.processLine(elements);
		String result = elements.getMarkdown();
		String encodedUrl = WidgetEncodingUtil.encodeValue("http://example.com");
		assertTrue(result.contains("${bookmark?text=this&inlineWidget=true&bookmarkID=subject1}"));
		assertTrue(result.contains("${link?text=here&url=" + encodedUrl + "&inlineWidget=true}"));
	}
}
