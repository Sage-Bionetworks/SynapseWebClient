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
		//Test different ordering of parameters
		String text = "The statement was from here ${reference?inlineWidget=true&text=Heidi}.";
		MarkdownElements elements = new MarkdownElements(text);
		parser.reset();
		parser.processLine(elements);
		assertTrue(elements.getHtml().contains("The statement was from here ${reference?inlineWidget=true&text=Heidi&footnoteId=1}."));
		
		String text2 = "The statement was from here ${reference?text=Heidi So%2E&inlineWidget=true}.";
		MarkdownElements elements2 = new MarkdownElements(text2);
		parser.processLine(elements2);
		assertTrue(elements2.getHtml().contains("The statement was from here ${reference?text=Heidi So%2E&inlineWidget=true&footnoteId=2}."));
		
		StringBuilder html = new StringBuilder("This is the last sentence.");
		parser.completeParse(html);
		String result = html.toString();
		assertTrue(result.contains("[1] Heidi"));
		assertTrue(result.contains("[2] Heidi So."));
	}
}