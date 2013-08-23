package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.SuperscriptParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;

public class SuperscriptParserTest {
	SuperscriptParser parser;
	
	@Before
	public void setup(){
		parser = new SuperscriptParser();
	}
	
	@Test
	public void testSuperscript(){
		String text = "10 in 2^10^ should be a superscript as should (x * 2) in 2^(x * 2)^.";
		MarkdownElements elements = new MarkdownElements(text);
		parser.processLine(elements, null);
		assertTrue(elements.getHtml().contains("<sup>10</sup>"));
		assertTrue(elements.getHtml().contains("<sup>(x * 2)</sup>"));
	}
}
