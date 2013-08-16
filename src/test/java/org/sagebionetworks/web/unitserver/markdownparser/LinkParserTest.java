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
		
		String text2 = "Synapse";
		String href2 = "#!Synapse:syn12345";
		String line2 = "[" + text2 + "](" + href2 +")";
		MarkdownElements elements2 = new MarkdownElements(line2);
		parser.processLine(elements2);
		String result2 = elements2.getHtml();
		assertTrue(result2.contains("href=\"#!Synapse:syn12345"));
		
		String text3 = "Synapse";
		String href3 = "#Synapse:syn12345";
		String line3 = "[" + text3 + "](" + href3 +")";
		MarkdownElements elements3 = new MarkdownElements(line3);
		parser.processLine(elements3);
		String result3 = elements3.getHtml();
		assertTrue(result3.contains("href=\"#Synapse:syn12345"));
	}
	
	@Test
	public void testIncompleteLinks() {
		String text = "Test";
		String href = "www.example.com";
		String line = "[" + text + "](" + href +")";
		MarkdownElements elements = new MarkdownElements(line);
		parser.processLine(elements);
		String result = elements.getHtml();
		assertTrue(result.contains("href=\"http://www.example.com"));
		
		String text2 = "Test";
		String href2 = "example.com";
		String line2 = "[" + text2 + "](" + href2 +")";
		MarkdownElements elements2 = new MarkdownElements(line2);
		parser.processLine(elements2);
		String result2 = elements2.getHtml();
		assertTrue(result2.contains("href=\"http://example.com"));
		
		String text3 = "Test";
		String href3 = "ftp://ftp.example";
		String line3 = "[" + text3 + "](" + href3 +")";
		MarkdownElements elements3 = new MarkdownElements(line3);
		parser.processLine(elements3);
		String result3 = elements3.getHtml();
		assertTrue(result3.contains("href=\"ftp://ftp.example"));
		
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
