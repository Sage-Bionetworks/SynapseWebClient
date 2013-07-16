package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.ReferenceParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;

public class ReferenceParserTest {
	ReferenceParser parser;
	
	@Before
	public void setup(){
		parser = new ReferenceParser();
	}
	
	@Test
	public void testReference(){
		String text = "The statement was from here ${reference?text=So H et al}.";
		MarkdownElements elements = new MarkdownElements(text);
		parser.reset();
		parser.processLine(elements);
		assertTrue(elements.getHtml().contains("The statement was from here ${reference?text=So H et al&footnoteId=1}."));
		
		StringBuilder html = new StringBuilder("This is the last sentence.");
		parser.completeParse(html);
		String result = html.toString();
		assertTrue(result.contains("[1] So H et al"));
	}
}
