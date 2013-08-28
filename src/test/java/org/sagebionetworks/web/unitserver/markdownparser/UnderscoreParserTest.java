package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;
import org.sagebionetworks.web.server.markdownparser.UnderscoreParser;
import org.sagebionetworks.web.server.markdownparser.ItalicsParser;

public class UnderscoreParserTest {
	UnderscoreParser parser;
	ItalicsParser italicsParser;
	
	@Before
	public void setup() {
		parser = new UnderscoreParser();
		italicsParser = new ItalicsParser();
	}
	
	@Test
	public void testEscaping() {
		String text = "file\\_number\\_1";
		MarkdownElements elements = new MarkdownElements(text);
		//Escape first, then use other parsers
		parser.processLine(elements);
		italicsParser.processLine(elements);
		assertEquals(elements.getHtml(), "file&#95;number&#95;1");
		assertFalse(elements.getHtml().contains("<em>number</em>"));
	}
}
