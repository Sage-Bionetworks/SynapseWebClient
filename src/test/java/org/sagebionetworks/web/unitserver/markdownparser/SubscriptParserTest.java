package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.SubscriptParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;

public class SubscriptParserTest {
	SubscriptParser parser;
	
	@Before
	public void setup(){
		parser = new SubscriptParser();
	}
	
	@Test
	public void testSubscript(){
		String text = "2 in H~2~0 should be a subscript as should x / 2 in log~x / 2~.";
		MarkdownElements elements = new MarkdownElements(text);
		parser.processLine(elements, null);
		assertTrue(elements.getHtml().contains("<sub>2</sub>"));
		assertTrue(elements.getHtml().contains("<sub>x / 2</sub>"));
	}
}
