package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.StrikeoutParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;

public class StrikeoutParserTest {
	StrikeoutParser parser;
	
	@Before
	public void setup(){
		parser = new StrikeoutParser();
	}
	
	@Test
	public void testStrikeout(){
		String text = "This is correct --not this part--.";
		MarkdownElements elements = new MarkdownElements(text);
		parser.processLine(elements);
		assertTrue(elements.getHtml().contains("<del>not this part</del>"));
	}
}
