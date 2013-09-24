package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.ServerMarkdownUtils;
import org.sagebionetworks.web.server.markdownparser.ItalicsParser;
import org.sagebionetworks.web.server.markdownparser.LinkParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElementParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;
import org.sagebionetworks.web.server.markdownparser.SynapseMarkdownWidgetParser;

public class LinkParserTest {
	LinkParser parser;
	List<MarkdownElementParser> simpleParsers;
	
	@Before
	public void setup(){	
		SynapseMarkdownWidgetParser widgetParser = new SynapseMarkdownWidgetParser();
		widgetParser.reset(null);
	
		ItalicsParser italicsParser = new ItalicsParser();
		
		simpleParsers = new ArrayList<MarkdownElementParser>();
		simpleParsers.add(widgetParser);
		simpleParsers.add(italicsParser);
		
		parser = new LinkParser();
		parser.reset(simpleParsers);
	}
	
	@Test
	public void testLink(){
		//text, with italicized "Is"
		String text = "This *Is* A Test";
		String href = "http://example.com";
		String line = "[" + text + "](" + href +")";
		MarkdownElements elements = new MarkdownElements(line);
		parser.processLine(elements);
		String result = elements.getHtml();
		assertTrue(!result.contains("http://example.com"));
		assertTrue(result.contains(ServerMarkdownUtils.START_CONTAINER));
		assertTrue(result.contains(ServerMarkdownUtils.END_CONTAINER));
		
		Document doc = Jsoup.parse(result);
		parser.completeParse(doc);
		String html = doc.html();
		assertTrue(html.contains("http://example.com"));
		assertTrue(html.contains("<a"));
		assertTrue(html.contains("</a>"));
		//simpleparsers has a the italics parser, so it should result in italicized text
		assertTrue(html.contains("<em"));
		assertTrue(html.contains("</em>"));
	}
	
	@Test
	public void testForCompleteness() {
		String text = "Test";
		String href = "example.com";
		String line = "[" + text + "](" + href +")";
		MarkdownElements elements = new MarkdownElements(line);
		parser.processLine(elements);
		String result = elements.getHtml();
		Document doc = Jsoup.parse(result);
		parser.completeParse(doc);
		assertTrue(doc.html().contains("http://example.com"));
		
		String text2 = "Test";
		String href2 = "ftp://ftp.example";
		String line2 = "[" + text2 + "](" + href2 +")";
		MarkdownElements elements2 = new MarkdownElements(line2);
		parser.processLine(elements2);
		String result2 = elements2.getHtml();
		Document doc2 = Jsoup.parse(result2);
		parser.completeParse(doc2);
		assertTrue(doc2.html().contains("ftp://ftp.example"));
		
	}
	
	@Test
	public void testBookmarkAndLink() {
		String line = "I want to refer to [this](#Bookmark:subject1). To see official page, go [here](http://example.com).";
		MarkdownElements elements = new MarkdownElements(line);
		parser.processLine(elements);
		String result = elements.getMarkdown();
		assertFalse(result.contains("${bookmark?text=this&inlineWidget=true&bookmarkID=subject1}"));
		assertTrue(result.contains("widgetsyntax-0"));
		assertFalse(result.contains("http://example.com"));
		assertTrue(result.contains("link-0"));
		assertTrue(result.contains(ServerMarkdownUtils.START_CONTAINER));
		assertTrue(result.contains(ServerMarkdownUtils.END_CONTAINER));
	}
	
}
