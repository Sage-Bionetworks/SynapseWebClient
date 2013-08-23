package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.ItalicsParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;

public class ItalicsParserTest {
	ItalicsParser parser;
	
	@Before
	public void setup(){
		parser = new ItalicsParser();
	}
	
	@Test
	public void testItalics(){
		String text = "*this* should be italicized, and so should _that_";
		MarkdownElements elements = new MarkdownElements(text);
		parser.processLine(elements, null);
		String result = elements.getHtml();
		assertTrue(result.contains("<em>this</em>"));
		assertTrue(result.contains("<em>that</em>"));
	}
}
