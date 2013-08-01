package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.server.markdownparser.ReferenceParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;
import org.sagebionetworks.web.shared.WebConstants;

public class ReferenceParserTest {
	ReferenceParser parser;
	
	@Before
	public void setup(){
		parser = new ReferenceParser();
	}
	
	@Test
	public void testReference(){
		//Test different ordering of parameters
		String text = "The statement was from here ${reference?inlineWidget=true&text=Smith John%2E Cooking book%2E August 2 2013&url=false}.";
		MarkdownElements elements = new MarkdownElements(text);
		parser.reset();
		parser.processLine(elements);
		assertTrue(elements.getHtml().contains("${reference?inlineWidget=true&text=Smith John%2E Cooking book%2E August 2 2013&url=false&footnoteId=1}."));
		
		String text2 = "The statement was from here ${reference?text=Smith John%2E Cooking book%2E August 2 2013&inlineWidget=true&url=false}.";
		MarkdownElements elements2 = new MarkdownElements(text2);
		parser.processLine(elements2);
		assertTrue(elements2.getHtml().contains("${reference?text=Smith John%2E Cooking book%2E August 2 2013&inlineWidget=true&url=false&footnoteId=2}."));
		
		StringBuilder html = new StringBuilder("This is the last sentence.");
		parser.completeParse(html);
		String result = html.toString();
		assertTrue(result.contains("[[1]](#Bookmark:wikiReference1)<p id=\"wikiFootnote1\" class=\"inlineWidgetContainer\">Smith John. Cooking book. August 2 2013</p>"));
		assertTrue(result.contains("[[2]](#Bookmark:wikiReference2)<p id=\"wikiFootnote2\" class=\"inlineWidgetContainer\">Smith John. Cooking book. August 2 2013</p>"));
	}
	
	public void testReferenceWithUrl() {
		String text = "The statement was from here ${reference?text=Smith John%2E Cooking book%2E August 2 2013&inlineWidget=true&url=false&url=http%3A%2F%2Fwww%2Eyahoo%2Ecom}.";
		MarkdownElements elements = new MarkdownElements(text);
		parser.reset();
		parser.processLine(elements);
		StringBuilder html = new StringBuilder("This is the last sentence.");
		parser.completeParse(html);
		String result = html.toString();
		assertTrue(result.contains("[[1]](#Bookmark:wikiReference1)<p id=\"wikiFootnote1\" class=\"inlineWidgetContainer\"></p>[Smith John. Cooking book. August 2 2013](http://www.yahoo.com)"));
		
	}
}